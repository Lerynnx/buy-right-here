package com.super_spring.gateway.model;

public record PaymentMethodDTO(
    long id,
    String name,
    String provider, // (Stripe, PayPal, Redsys…)
    boolean active
) {}