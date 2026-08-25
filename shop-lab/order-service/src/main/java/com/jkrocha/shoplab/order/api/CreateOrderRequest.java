package com.jkrocha.shoplab.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * Inbound payload for {@code POST /orders}. Monetary values are in minor units.
 */
public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotBlank String currency,
        @NotEmpty @Valid List<Item> items,
        @Valid ShippingAddress shippingAddress) {

    public record Item(
            @NotBlank String sku,
            @NotBlank String name,
            @Positive int quantity,
            @PositiveOrZero long unitPrice) {
    }

    public record ShippingAddress(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank String zipCode,
            @NotBlank String country) {
    }
}
