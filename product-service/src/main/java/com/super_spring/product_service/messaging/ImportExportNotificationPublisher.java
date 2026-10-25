package com.super_spring.product_service.messaging;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.super_spring.product_service.config.ApplicationConstants;

@Component
public class ImportExportNotificationPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportNotificationPublisher.class);

    private final PulsarTemplate<String> pulsarTemplate;
    private final ObjectMapper objectMapper;
    private final String completionTopic;
    private final MeterRegistry meterRegistry;

    public ImportExportNotificationPublisher(
            PulsarTemplate<String> pulsarTemplate,
            ObjectMapper objectMapper,
            @Value("${app.pulsar.import-export-completion-topic}") String completionTopic,
            MeterRegistry meterRegistry) {
        this.pulsarTemplate = pulsarTemplate;
        this.objectMapper = objectMapper;
        this.completionTopic = completionTopic;
        this.meterRegistry = meterRegistry;
    }

    public CompletableFuture<Void> publish(
            String operationId,
            String userId,
            String operationType,
            String status,
            String fileName) {
        ImportExportCompletionEvent event = new ImportExportCompletionEvent(
                UUID.randomUUID().toString(),
                operationId,
                userId,
                operationType,
                status,
                fileName,
                Instant.now(),
                MDC.get(ApplicationConstants.TRACE_ID_MDC_KEY));
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            meterRegistry.counter(ApplicationConstants.NOTIFICATION_METRIC,
                    ApplicationConstants.METRIC_OPERATION_TAG, operationType,
                    ApplicationConstants.RESULT_TAG, ApplicationConstants.FAILURE_RESULT).increment();
            LOGGER.error("Unable to serialize {} completion notification {}", operationType, operationId,
                    exception);
            return CompletableFuture.failedFuture(exception);
        }

        return pulsarTemplate.sendAsync(completionTopic, payload)
                .thenAccept(messageId -> {
                    meterRegistry.counter(ApplicationConstants.NOTIFICATION_METRIC,
                            ApplicationConstants.METRIC_OPERATION_TAG, operationType,
                            ApplicationConstants.RESULT_TAG, ApplicationConstants.SUCCESS_RESULT).increment();
                    LOGGER.info(ApplicationConstants.PUBLISHED_NOTIFICATION_LOG,
                            operationId, status, completionTopic);
                })
                .whenComplete((ignored, exception) -> {
                    if (exception != null) {
                        meterRegistry.counter(ApplicationConstants.NOTIFICATION_METRIC,
                                ApplicationConstants.METRIC_OPERATION_TAG, operationType,
                                ApplicationConstants.RESULT_TAG, ApplicationConstants.FAILURE_RESULT).increment();
                        LOGGER.error(ApplicationConstants.FAILED_NOTIFICATION_LOG,
                                operationId, completionTopic, exception);
                    }
                });
    }
}
