package com.buy_right_here.product_service.inventory.dto;

public record InventoryDTO (
    long id,
    long productId,
    int stock,
    boolean isActive
) {}