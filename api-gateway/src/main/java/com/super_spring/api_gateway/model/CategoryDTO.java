package com.super_spring.gateway.model;

public record CategoryDTO(
        long id,
        String name,
        String description,
        boolean active
) {}
