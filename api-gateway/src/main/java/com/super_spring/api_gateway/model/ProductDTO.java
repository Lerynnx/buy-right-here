package com.super_spring.gateway.model;

import java.time.Instant;

public record ProductDTO (
    long id,
    long categoryId,
    String name,
    String description,
    double price,
    double tax, //IVA percentage (e.g., 0.21 for 21% tax)
    int stock,
    Instant uploadingDate,
    List<String> imageUrls,
    String brand,
    Map<String, String> attributes,
    double rating,
    int totalReviews,
    boolean active
) {}