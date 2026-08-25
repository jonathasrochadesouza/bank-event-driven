package com.jkrocha.shoplab.logistic.processing;

import com.google.common.util.concurrent.RateLimiter;
import com.jkrocha.shoplab.logistic.event.OrderCreatedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Applies the rate limit, deduplicates by {@code eventId}, and "processes" the
 * order (simulated logistics work), recording throughput and latency metrics.
 */
@Component
@SuppressWarnings("UnstableApiUsage")
public class OrderProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);

    private final RateLimiter rateLimiter;
    private final ProcessedEventStore processedEventStore;
    private final Counter consumedCounter;
    private final Counter duplicateCounter;
    private final Timer processingTimer;

    public OrderProcessor(RateLimiter processingRateLimiter,
                          ProcessedEventStore processedEventStore,
                          MeterRegistry meterRegistry) {
        this.rateLimiter = processingRateLimiter;
        this.processedEventStore = processedEventStore;
        this.consumedCounter = Counter.builder("orders.consumed")
                .description("Number of order.created events successfully processed")
                .register(meterRegistry);
        this.duplicateCounter = Counter.builder("orders.duplicates")
                .description("Number of duplicate order.created events skipped")
                .register(meterRegistry);
        this.processingTimer = Timer.builder("orders.processing")
                .description("Order processing latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public void process(OrderCreatedEvent event) {
        // Block until a permit is available: this is what caps throughput and
        // lets consumer lag build under overload.
        rateLimiter.acquire();

        if (!processedEventStore.markIfNew(event.eventId())) {
            duplicateCounter.increment();
            log.debug("Skipping duplicate eventId={}", event.eventId());
            return;
        }

        processingTimer.record(() -> handle(event));
        consumedCounter.increment();
    }

    private void handle(OrderCreatedEvent event) {
        String orderId = event.data() != null ? event.data().orderId() : "unknown";
        // Simulated logistics work (create shipment, reserve stock, etc.).
        log.info("Processed order {} (eventId={})", orderId, event.eventId());
    }
}
