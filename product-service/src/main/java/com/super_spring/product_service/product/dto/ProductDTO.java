package com.super_spring.product_service.product.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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