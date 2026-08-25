package com.jkrocha.shoplab.order.kafka;

import com.jkrocha.shoplab.order.event.OrderCreatedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link OrderCreatedEvent} records keyed by {@code orderId} so that all
 * events for a given order land on the same partition (per-order ordering).
 */
@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String topic;
    private final Counter producedCounter;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
                              @Value("${app.topic.order-created}") String topic,
                              MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.producedCounter = Counter.builder("orders.produced")
                .description("Number of order.created events successfully produced")
                .register(meterRegistry);
    }

    public void publish(String orderId, OrderCreatedEvent event) {
        kafkaTemplate.send(topic, orderId, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish order {} to {}", orderId, topic, ex);
                return;
            }
            producedCounter.increment();
            log.debug("Published order {} (eventId={}) to {}", orderId, event.eventId(), topic);
        });
    }
}
