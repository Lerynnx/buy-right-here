package com.buy_right_here.inventory_service.messaging;

public record LabelUpdatedEvent(
    long productId,
    String stockLabel,
    String traceId) {}
