package com.buy_right_here.api_gateway.model;

public record LabelUpdatedEvent(
    long productId,
    String stockLabel) {}