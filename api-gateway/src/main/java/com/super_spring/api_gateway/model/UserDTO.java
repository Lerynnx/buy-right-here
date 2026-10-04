package com.super_spring.gateway.model;

import java.time.Instant;

public record UserDTO(
        long id,
        String idCard,
        String email,
        String phone,
        String name,
        String surname,
        String imageUrl,
        Instant registrationDate,
        boolean active
) {}