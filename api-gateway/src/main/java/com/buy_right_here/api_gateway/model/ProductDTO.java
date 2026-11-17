package com.buy_right_here.api_gateway.model;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDTO (
    long id,
    long categoryId,
    String name,
    String description,
    double price,
    double tax, //IVA percentage (e.g., 0.21 for 21% tax)
    Integer stock,
    String stockLabel,
    Instant uploadingDate,
    List<String> imageUrls,
    String brand,
    Map<String, String> attributes,
    double rating,
    int totalReviews,
    boolean active
) {}