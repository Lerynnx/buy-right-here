package com.buy_right_here.api_gateway.model;

public record PaymentMethodDTO(
    long id,
    String name,
    String provider, // (Stripe, PayPal, Redsys…)
    boolean active
) {}