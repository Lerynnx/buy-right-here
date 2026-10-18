package com.super_spring.product_service.service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.super_spring.product_service.event.ImportExportCompletionEvent;

@Component
public class ImportExportNotificationPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportNotificationPublisher.class);

    private final PulsarTemplate<String> pulsarTemplate;
    private final ObjectMapper objectMapper;
    private final String completionTopic;

    public ImportExportNotificationPublisher(
            PulsarTemplate<String> pulsarTemplate,
            ObjectMapper objectMapper,
            @Value("${app.pulsar.import-export-completion-topic}") String completionTopic) {
        this.pulsarTemplate = pulsarTemplate;
        this.objectMapper = objectMapper;
        this.completionTopic = completionTopic;
    }

    public CompletableFuture<Void> publish(String operation, String status) {
        ImportExportCompletionEvent event = new ImportExportCompletionEvent(
                operation, status, Instant.now(), MDC.get("traceId"));
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            return CompletableFuture.failedFuture(exception);
        }

        return pulsarTemplate.sendAsync(completionTopic, payload)
                .thenAccept(messageId -> LOGGER.info(
                        "Published {} completion notification with status {} to {}",
                        operation, status, completionTopic))
                .whenComplete((ignored, exception) -> {
                    if (exception != null) {
                        LOGGER.error("Failed to publish {} completion notification to {}",
                                operation, completionTopic, exception);
                    }
                });
    }
}
