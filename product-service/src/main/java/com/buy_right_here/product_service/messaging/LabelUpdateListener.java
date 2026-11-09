package com.buy_right_here.product_service.messaging;

import org.apache.pulsar.client.api.SubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import com.buy_right_here.product_service.config.ApplicationConstants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.buy_right_here.product_service.inventory.dto.LabelUpdatedEvent;
import com.buy_right_here.product_service.product.service.StockLabelService;

@Component
public class LabelUpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelUpdateListener.class);

    private final ObjectMapper objectMapper;
    private final StockLabelService stockLabelService;
    private final MeterRegistry meterRegistry;

    public LabelUpdateListener(ObjectMapper objectMapper, StockLabelService stockLabelService,
            MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.stockLabelService = stockLabelService;
        this.meterRegistry = meterRegistry;
    }

    @PulsarListener(
            topics = "${app.pulsar.inventory-update-topic}",
            subscriptionName = "${app.pulsar.inventory-update-subscription}",
            subscriptionType = SubscriptionType.Shared)
    public void onInventoryUpdated(String payload) {
        Timer.Sample sample = Timer.start(meterRegistry);
        final LabelUpdatedEvent event;
        try {
            event = objectMapper.readValue(payload, LabelUpdatedEvent.class);
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            meterRegistry.counter(ApplicationConstants.INVENTORY_EVENTS_METRIC,
                    ApplicationConstants.EVENT_TAG, ApplicationConstants.EVENT_INVENTORY_UPDATED,
                    ApplicationConstants.RESULT_TAG, ApplicationConstants.FAILURE_RESULT).increment();
            sample.stop(Timer.builder(ApplicationConstants.INVENTORY_EVENTS_METRIC + ".duration")
                    .description("Time spent processing inventory update events")
                    .tag(ApplicationConstants.EVENT_TAG, ApplicationConstants.EVENT_INVENTORY_UPDATED)
                    .register(meterRegistry));
            LOGGER.error("Unable to deserialize inventory update event", exception);
            throw new IllegalArgumentException("Invalid inventory update event", exception);
        }

        MDC.MDCCloseable traceContext = event.traceId() == null ? null
                : MDC.putCloseable(ApplicationConstants.TRACE_ID_MDC_KEY, event.traceId());
        try (traceContext) {
            try {
                stockLabelService.updateLabel(event.productId(), event.stockLabel());
                meterRegistry.counter(ApplicationConstants.INVENTORY_EVENTS_METRIC,
                        ApplicationConstants.EVENT_TAG, ApplicationConstants.EVENT_INVENTORY_UPDATED,
                        ApplicationConstants.RESULT_TAG, ApplicationConstants.SUCCESS_RESULT).increment();
                LOGGER.debug("Processed inventory update for product {} with trace {}", event.productId(),
                        event.traceId());
            } finally {
                sample.stop(Timer.builder(ApplicationConstants.INVENTORY_EVENTS_METRIC + ".duration")
                        .description("Time spent processing inventory update events")
                        .tag(ApplicationConstants.EVENT_TAG, ApplicationConstants.EVENT_INVENTORY_UPDATED)
                        .register(meterRegistry));
            }
        }
    }
}