package main.java.com.super_spring.api_gateway.model;

public record InventoryDTO (
    long id,
    long productId,
    int stock,
    boolean isActive
) {}