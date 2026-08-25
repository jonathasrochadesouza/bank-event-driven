package com.jkrocha.shoplab.logistic.kafka;

import com.jkrocha.shoplab.logistic.event.OrderCreatedEvent;
import com.jkrocha.shoplab.logistic.processing.OrderProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code order.created.v1} events for the {@code logistic-service} group
 * and delegates to {@link OrderProcessor} (rate limit + idempotency + metrics).
 */
@Component
public class OrderCreatedListener {

    private final OrderProcessor orderProcessor;

    public OrderCreatedListener(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @KafkaListener(topics = "${app.topic.order-created}", groupId = "logistic-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        orderProcessor.process(event);
    }
}
