package com.buy_right_here.inventory_service.inventory.dto;

public record InventoryDTO (
    long id,
    long productId,
    int stock,
    boolean isActive
) {}