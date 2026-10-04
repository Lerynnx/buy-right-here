package com.super_spring.gateway.model;

public record ShippingMethodDTO (
    long id,
    String name,
    double price,
    String description,
    boolean active
) {}