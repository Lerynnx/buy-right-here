package com.super_spring.product_service.product.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.super_spring.product_service.inventory.client.InventoryApiClient;
import com.super_spring.product_service.inventory.dto.InventoryDTO;
import com.super_spring.product_service.messaging.ImportExportNotificationPublisher;
import com.super_spring.product_service.config.ApplicationConstants;
import com.super_spring.product_service.product.dto.ExportStartResponse;
import com.super_spring.product_service.product.entity.Product;
import com.super_spring.product_service.product.repository.ProductRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class ImportExportProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportProductService.class);
    private static final Pattern CSV_FILE_NAME =
            Pattern.compile(ApplicationConstants.CSV_FILE_NAME_REGEX);

    private final ImportExportNotificationPublisher notificationPublisher;
    private final ProductRepository productRepository;
    private final InventoryApiClient inventoryApiClient;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final Executor exportTaskExecutor;
    private final Path exportDirectory;
    private final int pageSize;

    public ImportExportProductService(
            ImportExportNotificationPublisher notificationPublisher,
            ProductRepository productRepository,
            InventoryApiClient inventoryApiClient,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            @Qualifier(ApplicationConstants.EXPORT_TASK_EXECUTOR) Executor exportTaskExecutor,
            @Value("${app.export.directory}") String exportDirectory,
            @Value("${app.export.page-size}") int pageSize) {
        this.notificationPublisher = notificationPublisher;
        this.productRepository = productRepository;
        this.inventoryApiClient = inventoryApiClient;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.exportTaskExecutor = exportTaskExecutor;
        this.exportDirectory = Paths.get(exportDirectory).toAbsolutePath().normalize();
        this.pageSize = pageSize;
    }

    public ExportStartResponse startExport(String userId) {
        String operationId = UUID.randomUUID().toString();
        CompletableFuture.runAsync(() -> executeExport(operationId, userId), exportTaskExecutor);
        return new ExportStartResponse(operationId);
    }

    public ResponseEntity<Resource> download(String fileName, String userId) {
        if (!CSV_FILE_NAME.matcher(fileName).matches()) {
            return ResponseEntity.badRequest().build();
        }

        Path file = exportDirectory.resolve(fileName).normalize();
        Path ownerFile = exportDirectory.resolve(fileName + ApplicationConstants.OWNER_FILE_SUFFIX).normalize();
        if (!file.startsWith(exportDirectory) || !ownerFile.startsWith(exportDirectory)
                || !Files.isRegularFile(file) || !Files.isRegularFile(ownerFile)) {
            return ResponseEntity.notFound().build();
        }

        try {
            String owner = Files.readString(ownerFile, StandardCharsets.UTF_8).trim();
            if (!owner.equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(ApplicationConstants.CSV_MEDIA_TYPE))
                    .header(ApplicationConstants.CONTENT_DISPOSITION_HEADER,
                            ApplicationConstants.ATTACHMENT_CONTENT_DISPOSITION + fileName + "\"")
                    .body(new FileSystemResource(file));
        } catch (IOException exception) {
            LOGGER.error(ApplicationConstants.UNABLE_TO_READ_EXPORT_LOG, fileName, exception);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Async(ApplicationConstants.IMPORT_TASK_EXECUTOR)
    public CompletableFuture<Void> importProductsFromCsv(MultipartFile file) {
        return CompletableFuture.completedFuture(null);
    }

    private void executeExport(String operationId, String userId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String fileName = ApplicationConstants.EXPORT_FILE_PREFIX + operationId
                + ApplicationConstants.CSV_FILE_SUFFIX;
        Path output = exportDirectory.resolve(fileName);
        try {
            Files.createDirectories(exportDirectory);
            writeProducts(output);
            Files.writeString(exportDirectory.resolve(fileName + ApplicationConstants.OWNER_FILE_SUFFIX),
                    userId, StandardCharsets.UTF_8);
            meterRegistry.counter(ApplicationConstants.COMPLETED_METRIC,
                    ApplicationConstants.METRIC_OPERATION_TAG, ApplicationConstants.EXPORT_OPERATION).increment();
            notificationPublisher.publish(operationId, userId, ApplicationConstants.EXPORT_OPERATION,
                    ApplicationConstants.COMPLETED_STATUS, fileName);
        } catch (Exception exception) {
            meterRegistry.counter(ApplicationConstants.FAILED_METRIC,
                    ApplicationConstants.METRIC_OPERATION_TAG, ApplicationConstants.EXPORT_OPERATION).increment();
            deleteQuietly(output);
            LOGGER.error(ApplicationConstants.EXPORT_FAILED_LOG, operationId, exception);
            notificationPublisher.publish(operationId, userId, ApplicationConstants.EXPORT_OPERATION,
                    ApplicationConstants.FAILED_STATUS, null);
        } finally {
            sample.stop(Timer.builder(ApplicationConstants.DURATION_METRIC)
                    .description(ApplicationConstants.OPERATION_DURATION_DESCRIPTION)
                    .tag(ApplicationConstants.METRIC_OPERATION_TAG, ApplicationConstants.EXPORT_OPERATION)
                    .register(meterRegistry));
        }
    }

    private void writeProducts(Path output) throws IOException {
        InventoryCursor inventoryCursor = new InventoryCursor();
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(ApplicationConstants.CSV_HEADER);
            writer.newLine();
            int offset = 0;
            while (true) {
                List<Product> products = productRepository.getProductsWithPagination(offset, pageSize);
                if (products.isEmpty()) {
                    break;
                }
                for (Product product : products) {
                    writer.write(toCsvRow(product, inventoryCursor.stockFor(product.getId())));
                    writer.newLine();
                }
                offset += products.size();
                if (products.size() < pageSize) {
                    break;
                }
            }
        }
        while (inventoryCursor.hasNext()) {
            inventoryCursor.next();
        }
    }

    private final class InventoryCursor {
        private List<InventoryDTO> page = List.of();
        private int pageIndex;
        private int offset;

        private int stockFor(long productId) {
            while (hasNext() && peek().productId() < productId) {
                next();
            }
            if (hasNext() && peek().productId() == productId) {
                return next().stock();
            }
            return 0;
        }

        private boolean hasNext() {
            while (pageIndex >= page.size()) {
                page = inventoryApiClient.getInventories(offset, pageSize);
                pageIndex = 0;
                if (page == null || page.isEmpty()) {
                    page = List.of();
                    return false;
                }
                offset += page.size();
            }
            return true;
        }

        private InventoryDTO peek() {
            return page.get(pageIndex);
        }

        private InventoryDTO next() {
            InventoryDTO inventory = page.get(pageIndex);
            pageIndex++;
            return inventory;
        }
    }

    private String toCsvRow(Product product, int stock) throws JsonProcessingException {
        return String.join(",",
                csv(product.getId()), csv(product.getCategoryId()), csv(product.getName()),
                csv(product.getDescription()), csv(product.getPrice()), csv(product.getTax()),
                csv(product.getUploadingDate()), csvJson(product.getImageUrls()), csv(product.getBrand()),
                csvJson(product.getAttributes()), csv(product.getRating()), csv(product.getTotalReviews()),
                csv(product.isActive()), csv(stock));
    }

    private String csvJson(Object value) throws JsonProcessingException {
        return csv(value == null ? "" : objectMapper.writeValueAsString(value));
    }

    private String csv(Object value) {
        return csv(value == null ? "" : String.valueOf(value));
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @Scheduled(fixedRateString = "${app.export.cleanup-interval}")
    public void cleanExportFiles() {
        if (!Files.isDirectory(exportDirectory)) {
            return;
        }
        try (var files = Files.list(exportDirectory)) {
            files.filter(Files::isRegularFile).forEach(this::deleteQuietly);
        } catch (IOException exception) {
            LOGGER.error(ApplicationConstants.UNABLE_TO_CLEAN_EXPORT_DIRECTORY_LOG,
                    exportDirectory, exception);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            LOGGER.warn(ApplicationConstants.UNABLE_TO_DELETE_LOG, path, exception);
        }
    }
}
