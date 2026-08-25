package com.jkrocha.shoplab.order.event;

import java.time.Instant;

/**
 * Transport envelope for the {@code order.created.v1} event. Envelope metadata is
 * kept separate from the business payload ({@link OrderData}) to ease versioning,
 * routing and observability.
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
