package com.buy_right_here.api_gateway.model;

public record CategoryDTO(
        long id,
        String name,
        String description,
        boolean active
) {}
