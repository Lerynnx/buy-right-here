package com.buy_right_here.api_gateway.model;

public record OrderLineDTO (
    long id,
    long orderId,
    long productId,
    double unitPrice,
    int quantity,
    double tax, //IVA percentage (e.g., 0.21 for 21% tax)
    double total
) {}