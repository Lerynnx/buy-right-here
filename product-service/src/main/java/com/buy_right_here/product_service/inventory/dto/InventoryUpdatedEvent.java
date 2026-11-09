package com.buy_right_here.product_service.inventory.dto;

public record InventoryUpdatedEvent(
    long productId,
    String stockLabel,
    String traceId) {}