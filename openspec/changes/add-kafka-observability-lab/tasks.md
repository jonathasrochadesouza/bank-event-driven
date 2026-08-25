## 1. Lab scaffolding

- [x] 1.1 Create `shop-lab/` directory structure: `order-service/`, `logistic-service/`, `observability/`, `loadtest/`
- [x] 1.2 Add a root `README.md` for the lab with up/down commands and access URLs (kafka-ui, Grafana, Prometheus, Tempo)
- [x] 1.3 Add `.env` (or compose defaults) for ports, bootstrap servers, topic name, partitions, and OTLP endpoint
- [ ] 1.4 Download the OpenTelemetry Java Agent jar into `shop-lab/observability/agent/` and document its version _(deferred to Phase 3 — only needed when wiring tracing)_

## 2. Messaging infrastructure (Phase 1)

- [x] 2.1 Add Kafka (KRaft, single broker) service to `docker-compose.yml` with a lab network and healthcheck
- [x] 2.2 Add an init container that creates topic `order.created.v1` idempotently with the configured partition count
- [x] 2.3 Add `kafka-ui` service connected to the broker
- [x] 2.4 Verify: stack starts with no Zookeeper, topic exists, and kafka-ui shows the topic

## 3. order-service producer (Phase 2)

- [ ] 3.1 Scaffold Spring Boot `order-service` with `spring-kafka` and `spring-boot-starter-web`
- [ ] 3.2 Define the JSON event envelope + payload model (eventId, eventType, eventVersion, occurredAt, source, data with money in minor units)
- [ ] 3.3 Implement `POST /orders` with request validation returning the generated `orderId`
- [ ] 3.4 Implement the producer: publish `order.created.v1` keyed by `orderId` using JSON serialization
- [ ] 3.5 Add Micrometer `orders_produced_total` counter incremented on successful publish
- [ ] 3.6 Verify end-to-end: a POST produces one readable JSON record on the topic (inspect in kafka-ui)

## 4. logistic-service consumer (Phase 2)

- [ ] 4.1 Scaffold Spring Boot `logistic-service` with `spring-kafka`
- [ ] 4.2 Implement `@KafkaListener` on `order.created.v1` bound to the `logistic-service` consumer group with JSON deserialization
- [ ] 4.3 Add in-application rate limiter (Resilience4j or Guava) permitting 50 msg/s per instance; acquire before processing
- [ ] 4.4 Add idempotency: deduplicate by `eventId` so reprocessing is a no-op
- [ ] 4.5 Add Micrometer `orders_consumed_total` counter and a processing-latency timer
- [ ] 4.6 Verify: events flow producer → consumer end to end and offsets advance

## 5. Distributed tracing (Phase 3)

- [ ] 5.1 Attach the OTel Java Agent to both services via `JAVA_TOOL_OPTIONS`/entrypoint and set OTLP endpoint + service names
- [ ] 5.2 Add OpenTelemetry Collector service with `otlp` receiver, `batch` processor, and exporters to Tempo (traces) and Prometheus (metrics)
- [ ] 5.3 Add Tempo service and config; wire it as a Grafana data source
- [ ] 5.4 Ensure consumer-side trace propagation (agent auto-propagation; enable `observationEnabled(true)` if needed)
- [ ] 5.5 Verify: a single trace in Grafana/Tempo spans HTTP → produce → consume → processing with one shared trace id

## 6. Throughput metrics and dashboards (Phase 4)

- [ ] 6.1 Add Prometheus service with `scrape_interval: 1s` and targets (Collector metrics, kafka-exporter)
- [ ] 6.2 Add `kafka-exporter` service exposing consumer lag per partition for the `logistic-service` group
- [ ] 6.3 Add Grafana with provisioned data sources (Prometheus, Tempo) and a lab dashboard: produced/s, consumed/s, consumer lag, latency percentiles
- [ ] 6.4 Verify: dashboards update in near real time while traffic flows

## 7. Load testing (Phase 5)

- [ ] 7.1 Create a JMeter `.jmx` plan issuing `POST /orders` with a configurable request rate (thread group + throughput timer)
- [ ] 7.2 Parameterize target host/port and request rate
- [ ] 7.3 Verify: running the plan above 50 req/s makes produced/s exceed consumed/s and lag rise in Grafana

## 8. Backpressure exercise and docs (Phase 6)

- [ ] 8.1 Run an overload scenario (rate > 50/s); capture the growing-lag behavior and recovery when load drops
- [ ] 8.2 Demonstrate the broken-trace case (propagation disabled) vs the correct single trace, and document the fix
- [ ] 8.3 Document consumer tuning notes (max.poll.records, concurrency) and the per-instance nature of the 50 msg/s limit
- [ ] 8.4 Verify full lab lifecycle: `docker compose up` brings everything healthy; `docker compose down -v` cleanly resets state
