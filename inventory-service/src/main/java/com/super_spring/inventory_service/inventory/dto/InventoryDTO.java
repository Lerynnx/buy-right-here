package com.super_spring.inventory_service.inventory.dto;

public record InventoryDTO (
    long id,
    long productId,
    int stock,
    boolean isActive
) {}