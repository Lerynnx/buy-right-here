package main.java.com.super_spring.api_gateway.model;

public record LabelUpdatedEvent(
    long productId,
    String stockLabel) {}