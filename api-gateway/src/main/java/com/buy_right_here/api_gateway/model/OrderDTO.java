package com.buy_right_here.api_gateway.model;

import java.time.Instant;
import java.util.List;

public record OrderDTO (
    long id,
    long userId,
    Instant date,
    String paymentMethod,
    String paymentStatus,
    String billingAddress, // direccion de facturacion
    String shippingAddress,
    String shippingMethod, //tipo de envio (estandar, express, etc.) en funcion de esto sera mas caro o mas barato el envio
    double subtotal, // suma del dinero de cada OrderLine
    double taxAmount, //suma del dinero de impuestos de cada OrderLine
    double shippingCost, // en funcion del tipo de envio
    String carrier, //empresa de transporte, random entre DHL, FedEx, UPS, etc.
    String notes,
    double totalPrice, // subtotal + taxAmount + shippingCost

    List<OrderLineDTO> orderLines
) {}