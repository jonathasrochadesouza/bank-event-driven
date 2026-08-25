package com.jkrocha.shoplab.logistic.event;

import java.time.Instant;

/**
 * Consumer-side view of the {@code order.created.v1} envelope. Kept intentionally
 * independent from the producer's class so the services stay decoupled; unknown
 * fields are ignored during deserialization.
 */
public record OrderCreatedEvent(
        String eventId,
        String eventType,
        String eventVersion,
        Instant occurredAt,
        String source,
        String traceId,
        OrderData data) {
}
