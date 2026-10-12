package com.super_spring.product_service.model.dto;

public record InventoryDTO (
    long id,
    long productId,
    int stock
) {}