package com.jkrocha.shoplab.order.service;

import com.jkrocha.shoplab.order.api.CreateOrderRequest;
import com.jkrocha.shoplab.order.event.OrderCreatedEvent;
import com.jkrocha.shoplab.order.event.OrderData;
import com.jkrocha.shoplab.order.kafka.OrderEventProducer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Turns an inbound order request into an {@code order.created.v1} event and
 * publishes it. The generated {@code orderId} is returned to the caller.
 */
@Service
public class OrderService {

    private static final String EVENT_TYPE = "order.created";
    private static final String EVENT_VERSION = "1.0";
    private static final String SOURCE = "order-service";

    private final OrderEventProducer producer;
    private final AtomicLong sequence = new AtomicLong();

    public OrderService(OrderEventProducer producer) {
        this.producer = producer;
    }

    public String createOrder(CreateOrderRequest request) {
        Instant now = Instant.now();
        String orderId = generateOrderId();

        List<OrderData.OrderItem> items = request.items().stream()
                .map(i -> new OrderData.OrderItem(i.sku(), i.name(), i.quantity(), i.unitPrice()))
                .toList();

        long totalAmount = items.stream()
                .mapToLong(i -> i.unitPrice() * i.quantity())
                .sum();

        OrderData.ShippingAddress address = request.shippingAddress() == null ? null
                : new OrderData.ShippingAddress(
                request.shippingAddress().street(),
                request.shippingAddress().city(),
                request.shippingAddress().state(),
                request.shippingAddress().zipCode(),
                request.shippingAddress().country());

        OrderData data = new OrderData(
                orderId,
                request.customerId(),
                "CREATED",
                request.currency(),
                totalAmount,
                items,
                address,
                now);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                EVENT_VERSION,
                now,
                SOURCE,
                null, // populated via the OTel trace header in Phase 3
                data);

        producer.publish(orderId, event);
        return orderId;
    }

    private String generateOrderId() {
        return "ORD-%d-%06d".formatted(Year.now().getValue(), sequence.incrementAndGet());
    }
}
