## Why

We need a hands-on, local laboratory to learn and validate event-driven messaging with Apache Kafka: producing/consuming events, load testing with JMeter, tuning consumer throughput, and observing the system end-to-end (distributed traces + throughput metrics). The two reference projects in this workspace (`eg-ecommerce`, `eg-yas-shop`) demonstrate messaging and the OTel→Tempo/Prometheus/Grafana stack, but neither includes load testing nor throughput/lag instrumentation, which are the core of this exercise. A dedicated, minimal `shop` lab lets us feel Kafka behavior under controlled backpressure without the weight of a full platform.

## What Changes

- Add a new self-contained lab (domain: `shop`) runnable entirely via Docker Compose.
- Add `order-service` (Spring Boot): exposes `POST /orders`, publishes `order.created.v1` events to Kafka keyed by `orderId`, using a JSON envelope + payload.
- Add `logistic-service` (Spring Boot): consumes `order.created.v1` via `@KafkaListener`, applies an in-application rate limiter capped at 50 messages/second per instance, and deduplicates by `eventId` (at-least-once handling).
- Add single-broker Apache Kafka in **KRaft mode** (no Zookeeper), topic auto-creation for `order.created.v1`, and `kafka-ui` for topic inspection.
- Add distributed tracing via the **OpenTelemetry Java Agent** (zero code) on both services, exporting OTLP to an **OpenTelemetry Collector**, with **Tempo** as the trace backend. Trace context propagates across Kafka (HTTP → produce → consume).
- Add metrics via **Micrometer** exported through the Collector to **Prometheus** (scrape interval `1s`), plus `kafka-exporter` for consumer lag per partition, visualized in **Grafana** (produced/s, consumed/s, lag, latency).
- Add a **JMeter** test plan that drives load via REST against `order-service` to exceed 50 msg/s and produce observable consumer lag, plus a same-origin browser page for manually dispatching a chosen number of orders.
- Use **JSON** serialization for events in the lab (Schema Registry / Avro noted as future evolution, out of scope).

## Capabilities

### New Capabilities
- `messaging-infrastructure`: Local Kafka (KRaft, 1 broker), topic `order.created.v1`, JSON event envelope/schema contract, and `kafka-ui`, all orchestrated by Docker Compose.
- `order-event-production`: `order-service` REST intake and publishing of `order.created.v1` events with correct keying and schema.
- `logistics-event-consumption`: `logistic-service` consumption with 50 msg/s in-application rate limiting and idempotent handling.
- `distributed-tracing`: OpenTelemetry Java Agent instrumentation, Collector, and Tempo delivering a single end-to-end trace across HTTP and Kafka.
- `throughput-metrics`: Micrometer + Prometheus (1s scrape) + kafka-exporter + Grafana dashboards for produced/s, consumed/s, and consumer lag.
- `load-testing`: JMeter REST-based load plan to generate configurable request rates against `order-service`, plus a browser bulk-order launcher for interactive experiments.

### Modified Capabilities
<!-- None. This is a greenfield lab; no existing specs change. -->

## Impact

- New top-level lab directory (proposed `shop-lab/`) containing two Spring Boot services, Docker Compose files, OTel Collector / Prometheus / Tempo / Grafana configs, and a JMeter plan. No changes to existing `eg-*` projects.
- New dependencies (container images): `confluentinc/cp-kafka` (KRaft), `provectuslabs/kafka-ui`, `otel/opentelemetry-collector-contrib`, `grafana/tempo`, `prom/prometheus`, `grafana/grafana`, `danielqsj/kafka-exporter`, plus the OpenTelemetry Java Agent jar.
- Local-only footprint sized for a learning lab (single broker, no replication). Not production-hardened; security (auth/TLS) and Schema Registry are explicitly out of scope.
