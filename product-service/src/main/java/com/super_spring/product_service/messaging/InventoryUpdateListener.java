package com.super_spring.product_service.messaging;

import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.super_spring.product_service.inventory.dto.InventoryUpdatedEvent;
import com.super_spring.product_service.product.service.StockLabelService;

@Component
public class InventoryUpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryUpdateListener.class);

    private final ObjectMapper objectMapper;
    private final StockLabelService stockLabelService;

    public InventoryUpdateListener(ObjectMapper objectMapper, StockLabelService stockLabelService) {
        this.objectMapper = objectMapper;
        this.stockLabelService = stockLabelService;
    }

    @PulsarListener(
            topics = "${app.pulsar.inventory-update-topic}",
            subscriptionName = "${app.pulsar.inventory-update-subscription}",
            subscriptionType = SubscriptionType.Shared)
    public void onInventoryUpdated(String payload) {
        final InventoryUpdatedEvent event;
        try {
            event = objectMapper.readValue(payload, InventoryUpdatedEvent.class);
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            LOGGER.error("Unable to deserialize inventory update event: {}", payload, exception);
            throw new IllegalArgumentException("Invalid inventory update event", exception);
        }

        stockLabelService.updateLabel(event.productId(), event.stockLabel());
    }
}
