package com.jkrocha.shoplab.logistic.event;

import java.time.Instant;
import java.util.List;

/** Business payload of an order. Monetary values are in integer minor units. */
public record OrderData(
        String orderId,
        String customerId,
        String status,
        String currency,
        long totalAmount,
        List<OrderItem> items,
        ShippingAddress shippingAddress,
        Instant createdAt) {

    public record OrderItem(
            String sku,
            String name,
            int quantity,
            long unitPrice) {
    }

    public record ShippingAddress(
            String street,
            String city,
            String state,
            String zipCode,
            String country) {
    }
}
