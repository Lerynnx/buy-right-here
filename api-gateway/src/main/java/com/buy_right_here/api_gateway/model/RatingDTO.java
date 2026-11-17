package com.buy_right_here.api_gateway.model;

import java.time.Instant;

public record RatingDTO (
    long id,
    long userId,
    long productId,
    String comment,
    Instant date,
    double rate,
    boolean active
) {}
