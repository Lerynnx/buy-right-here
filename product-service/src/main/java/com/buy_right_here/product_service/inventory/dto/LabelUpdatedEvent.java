package com.buy_right_here.product_service.inventory.dto;

public record LabelUpdatedEvent(
    long productId,
    String stockLabel,
    String traceId) {}