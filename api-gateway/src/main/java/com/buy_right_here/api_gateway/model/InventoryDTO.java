package com.buy_right_here.api_gateway.model;

public record InventoryDTO (
    long id,
    long productId,
    int stock,
    boolean isActive
) {}