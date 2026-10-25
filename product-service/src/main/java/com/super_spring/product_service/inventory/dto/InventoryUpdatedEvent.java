package com.super_spring.product_service.inventory.dto;

public record InventoryUpdatedEvent(long productId, String stockLabel) {
}
