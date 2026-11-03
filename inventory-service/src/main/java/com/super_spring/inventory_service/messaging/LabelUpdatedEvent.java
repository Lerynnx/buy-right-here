package com.super_spring.inventory_service.messaging;

public record LabelUpdatedEvent(
    long productId,
    String stockLabel) {}
