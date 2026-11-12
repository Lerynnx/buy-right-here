package com.buy_right_here.product_service.product.service;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.IntStream;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.buy_right_here.product_service.inventory.client.InventoryApiClient;
import com.buy_right_here.product_service.inventory.dto.InventoryDTO;
import com.buy_right_here.product_service.messaging.ImportExportNotificationPublisher;
import com.buy_right_here.product_service.config.ApplicationConstants;
import com.buy_right_here.product_service.product.dto.ExportStartResponse;
import com.buy_right_here.product_service.product.dto.ImportStartResponse;
import com.buy_right_here.product_service.product.entity.Product;
import com.buy_right_here.product_service.product.repository.ProductRepository;

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
    private final Executor importTaskExecutor;
    private final Path exportDirectory;
    private final int pageSize;
    private final int importBatchSize;
    private final String inventoryBatchPath;

    public ImportExportProductService(
            ImportExportNotificationPublisher notificationPublisher,
            ProductRepository productRepository,
            InventoryApiClient inventoryApiClient,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            @Qualifier(ApplicationConstants.EXPORT_TASK_EXECUTOR) Executor exportTaskExecutor,
            @Qualifier(ApplicationConstants.IMPORT_TASK_EXECUTOR) Executor importTaskExecutor,
            @Value("${app.export.directory}") String exportDirectory,
            @Value("${app.export.page-size}") int pageSize,
            @Value("${app.inventory.import-batch-size:"
                    + ApplicationConstants.DEFAULT_IMPORT_BATCH_SIZE + "}") int importBatchSize,
            @Value("${app.inventory.batch-path:" + ApplicationConstants.INVENTORY_BATCH_PATH
                    + "}") String inventoryBatchPath) {
        this.notificationPublisher = notificationPublisher;
        this.productRepository = productRepository;
        this.inventoryApiClient = inventoryApiClient;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.exportTaskExecutor = exportTaskExecutor;
        this.importTaskExecutor = importTaskExecutor;
        this.exportDirectory = Paths.get(exportDirectory).toAbsolutePath().normalize();
        this.pageSize = pageSize;
        if (importBatchSize <= 0) {
            throw new IllegalArgumentException("Import batch size must be greater than zero");
        }
        this.importBatchSize = importBatchSize;
        this.inventoryBatchPath = inventoryBatchPath;
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

    public ImportStartResponse startImport(MultipartFile file, String userId) {
        final byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read import file", exception);
        }
        String operationId = UUID.randomUUID().toString();
        CompletableFuture.runAsync(
                () -> executeImport(operationId, userId, file.getOriginalFilename(), content),
                importTaskExecutor);
        return new ImportStartResponse(operationId);
    }

    private void executeImport(String operationId, String userId, String fileName, byte[] content) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            importCsv(content);
            meterRegistry.counter(ApplicationConstants.COMPLETED_METRIC,
                    ApplicationConstants.METRIC_OPERATION_TAG, ApplicationConstants.IMPORT_OPERATION).increment();
            notificationPublisher.publish(operationId, userId, ApplicationConstants.IMPORT_OPERATION,
                    ApplicationConstants.COMPLETED_STATUS, fileName);
        } catch (Exception exception) {
            meterRegistry.counter(ApplicationConstants.FAILED_METRIC,
                    ApplicationConstants.METRIC_OPERATION_TAG, ApplicationConstants.IMPORT_OPERATION).increment();
            LOGGER.error(ApplicationConstants.IMPORT_FAILED_LOG, operationId, exception);
            notificationPublisher.publish(operationId, userId, ApplicationConstants.IMPORT_OPERATION,
                    ApplicationConstants.FAILED_STATUS, fileName);
        } finally {
            sample.stop(Timer.builder(ApplicationConstants.DURATION_METRIC)
                    .description(ApplicationConstants.OPERATION_DURATION_DESCRIPTION)
                    .tag(ApplicationConstants.METRIC_OPERATION_TAG, ApplicationConstants.IMPORT_OPERATION)
                    .register(meterRegistry));
        }
    }

    private void importCsv(byte[] content) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(content), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.replace("\uFEFF", "").equals(ApplicationConstants.CSV_HEADER)) {
                throw new IllegalArgumentException("Invalid product import CSV header");
            }

            List<ImportRecord> batch = new ArrayList<>(importBatchSize);
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    batch.add(parseImportRecord(parseCsvLine(line)));
                } catch (RuntimeException exception) {
                    throw new IllegalArgumentException("Invalid CSV row " + rowNumber, exception);
                }
                if (batch.size() == importBatchSize) {
                    processImportBatch(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                processImportBatch(batch);
            }
        }
    }

    private ImportRecord parseImportRecord(List<String> columns) {
        if (columns.size() != 14) {
            throw new IllegalArgumentException("Expected 14 columns but found " + columns.size());
        }
        Product product = new Product(
                parseLong(columns.get(0)),
                Long.parseLong(columns.get(1)),
                nullable(columns.get(2)),
                nullable(columns.get(3)),
                Double.parseDouble(columns.get(4)),
                Double.parseDouble(columns.get(5)),
                null,
                parseInstant(columns.get(6)),
                parseJson(columns.get(7), new TypeReference<List<String>>() {}),
                nullable(columns.get(8)),
                parseJson(columns.get(9), new TypeReference<java.util.Map<String, String>>() {}),
                Double.parseDouble(columns.get(10)),
                Integer.parseInt(columns.get(11)),
                Boolean.parseBoolean(columns.get(12)));
        return new ImportRecord(product, Integer.parseInt(columns.get(13)));
    }

    private void processImportBatch(List<ImportRecord> records) {
        meterRegistry.summary(ApplicationConstants.BATCH_SIZE_METRIC).record(records.size());
        LOGGER.debug("Processing product import batch with {} records", records.size());
        List<Product> products = records.stream().map(ImportRecord::product).toList();
        List<Product> savedProducts = productRepository.saveAll(products);
        List<InventoryDTO> inventories = new ArrayList<>(savedProducts.size());
        IntStream.range(0, savedProducts.size()).forEach(index -> {
            Product product = savedProducts.get(index);
            inventories.add(new InventoryDTO(0, product.getId(),
                    records.get(index).stock(), product.isActive()));
        });
        if (!inventories.isEmpty()) {
            inventoryApiClient.createOrUpdateInventories(inventories, inventoryBatchPath);
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                columns.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted value");
        }
        columns.add(value.toString());
        return columns;
    }

    private long parseLong(String value) {
        return value.isBlank() ? 0 : Long.parseLong(value);
    }

    private String nullable(String value) {
        return value.isBlank() ? null : value;
    }

    private java.time.Instant parseInstant(String value) {
        return value.isBlank() ? null : java.time.Instant.parse(value);
    }

    private <T> T parseJson(String value, TypeReference<T> type) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid JSON value", exception);
        }
    }

    private record ImportRecord(Product product, int stock) {
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
            LOGGER.debug("Export directory {} does not exist; skipping cleanup", exportDirectory);
            return;
        }
        try (var files = Files.list(exportDirectory)) {
            long deletedFiles = files.filter(Files::isRegularFile).peek(this::deleteQuietly).count();
            LOGGER.info("Cleaned {} files from export directory {}", deletedFiles, exportDirectory);
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
