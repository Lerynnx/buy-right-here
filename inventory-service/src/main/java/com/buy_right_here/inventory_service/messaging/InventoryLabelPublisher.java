package com.buy_right_here.inventory_service.messaging;

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
import com.buy_right_here.inventory_service.config.ApplicationConstants;

@Component
public class InventoryLabelPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryLabelPublisher.class);

    private final PulsarTemplate<String> pulsarTemplate;
    private final ObjectMapper objectMapper;
    private final String inventoryUpdateTopic;
    private final MeterRegistry meterRegistry;

    public InventoryLabelPublisher(
            PulsarTemplate<String> pulsarTemplate,
            ObjectMapper objectMapper,
            @Value("${app.pulsar.inventory-update-topic}") String inventoryUpdateTopic,
            MeterRegistry meterRegistry) {
        this.pulsarTemplate = pulsarTemplate;
        this.objectMapper = objectMapper;
        this.inventoryUpdateTopic = inventoryUpdateTopic;
        this.meterRegistry = meterRegistry;
    }

    public CompletableFuture<Void> publish(long productId, String stockLabel) {
        String traceId = MDC.get(ApplicationConstants.TRACE_ID_MDC_KEY);
        LabelUpdatedEvent event = new LabelUpdatedEvent(productId, stockLabel, traceId);
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            meterRegistry.counter(ApplicationConstants.INVENTORY_EVENTS_METRIC,
                    ApplicationConstants.EVENT_TAG, ApplicationConstants.EVENT_INVENTORY_UPDATED,
                    ApplicationConstants.RESULT_TAG, ApplicationConstants.FAILURE_RESULT).increment();
            LOGGER.error("Unable to serialize inventory update for product {}", productId, exception);
            return CompletableFuture.failedFuture(exception);
        }

        return pulsarTemplate.sendAsync(inventoryUpdateTopic, payload)
                .thenAccept(messageId -> {
                    meterRegistry.counter(ApplicationConstants.INVENTORY_EVENTS_METRIC,
                            ApplicationConstants.EVENT_TAG, ApplicationConstants.EVENT_INVENTORY_UPDATED,
                            ApplicationConstants.RESULT_TAG, ApplicationConstants.SUCCESS_RESULT).increment();
                    LOGGER.info("Published inventory update for product {} to {}", productId, inventoryUpdateTopic);
                })
                .whenComplete((ignored, exception) -> {
                    if (exception != null) {
                        meterRegistry.counter(ApplicationConstants.INVENTORY_EVENTS_METRIC,
                                ApplicationConstants.EVENT_TAG, ApplicationConstants.EVENT_INVENTORY_UPDATED,
                                ApplicationConstants.RESULT_TAG, ApplicationConstants.FAILURE_RESULT).increment();
                        LOGGER.error("Failed to publish inventory update for product {} to {}",
                                productId, inventoryUpdateTopic, exception);
                    }
                });
    }
}
