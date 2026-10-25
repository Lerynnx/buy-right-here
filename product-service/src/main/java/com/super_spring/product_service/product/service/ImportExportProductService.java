package com.super_spring.product_service.product.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.super_spring.product_service.messaging.ImportExportNotificationPublisher;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class ImportExportProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportProductService.class);

    private final ImportExportNotificationPublisher notificationPublisher;
    private final MeterRegistry meterRegistry;

    public ImportExportProductService(
            ImportExportNotificationPublisher notificationPublisher,
            MeterRegistry meterRegistry) {
        this.notificationPublisher = notificationPublisher;
        this.meterRegistry = meterRegistry;
    }

    @Async("exportTaskExecutor")
    public CompletableFuture<Void> exportProductsToCsv() {
        return execute("export");
    }


    @Async("importTaskExecutor")
    public CompletableFuture<Void> importProductsFromCsv(MultipartFile file) {
        return execute("import");
    }

    private CompletableFuture<Void> execute(String operation) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            LOGGER.info("Starting product {} operation", operation);
            // Product import/export work will be added here.
            meterRegistry.counter("product.import_export.completed", "operation", operation).increment();
            return notificationPublisher.publish(operation, "COMPLETED")
                    .whenComplete((ignored, exception) -> recordDuration(sample, operation, exception));
        } catch (RuntimeException exception) {
            meterRegistry.counter("product.import_export.failed", "operation", operation).increment();
            return notificationPublisher.publish(operation, "FAILED")
                    .handle((ignored, notificationException) -> {
                        if (notificationException != null) {
                            LOGGER.error("Failed to publish failure notification for {}", operation,
                                    notificationException);
                        }
                        throw exception;
                    });
        }
    }

    private void recordDuration(Timer.Sample sample, String operation, Throwable exception) {
        String status = exception == null ? "completed" : "failed";
        if (exception != null) {
            meterRegistry.counter("product.import_export.failed", "operation", operation).increment();
        }
        sample.stop(Timer.builder("product.import_export.duration")
                .description("Duration of product import and export operations")
                .tag("operation", operation)
                .tag("status", status)
                .register(meterRegistry));
    }
}