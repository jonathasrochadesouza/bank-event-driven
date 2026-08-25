package com.jkrocha.shoplab.logistic.processing;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tracks already-processed {@code eventId}s to make consumption idempotent under
 * Kafka's at-least-once delivery. This is an in-memory, per-instance store bounded
 * to a fixed number of recent ids (adequate for the lab, not for production).
 */
@Component
public class ProcessedEventStore {

    private static final int MAX_ENTRIES = 100_000;

    private final Set<String> processed = Collections.newSetFromMap(
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > MAX_ENTRIES;
                }
            }));

    /**
     * Registers the event id.
     *
     * @return {@code true} if this id is new (should be processed), {@code false}
     *         if it was already seen (duplicate).
     */
    public boolean markIfNew(String eventId) {
        return processed.add(eventId);
    }
}
