package com.buy_right_here.api_gateway.model;

public record ShippingMethodDTO (
    long id,
    String name,
    double price,
    String description,
    boolean active
) {}