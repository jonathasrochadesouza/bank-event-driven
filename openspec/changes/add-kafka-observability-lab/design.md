## Context

This is a greenfield learning lab in the `shop` domain, built to exercise event-driven messaging end-to-end. It is inspired by two reference projects in the workspace: `eg-ecommerce` (simple explicit `KafkaTemplate` producer + `@KafkaListener` consumer, plus OTel→Prometheus/Tempo/Grafana) and `eg-yas-shop` (Debezium CDC + full observability). The lab deliberately adopts the simpler explicit producer/consumer style and adds what neither reference has: JMeter load testing and throughput/consumer-lag instrumentation.

Constraints:
- Local-only, single developer machine. Keep the footprint small (single Kafka broker).
- Zero-code tracing is required (OpenTelemetry Java Agent).
- Consumer read rate must be capped at 50 messages/second per service instance to create observable backpressure.
- Prometheus scrape interval of 1s for near-real-time dashboards.
- JSON serialization for readability in `kafka-ui`.

## Goals / Non-Goals

**Goals:**
- Demonstrate a single distributed trace spanning `HTTP POST /orders → Kafka produce → Kafka consume → processing` using the OTel Java Agent and Tempo.
- Produce and consume `order.created.v1` events with a well-formed JSON envelope + payload following market best practices (idempotency key, UTC ISO-8601 timestamps, money in minor units, event/topic versioning, partition key).
- Cap consumer throughput at 50 msg/s per instance via an in-application rate limiter and make the resulting lag visible in Grafana.
- Drive configurable load through JMeter over REST and observe produced/s, consumed/s, latency, and consumer lag in near real time (1s scrape).
- Keep everything reproducible via Docker Compose.

**Non-Goals:**
- No Schema Registry / Avro / Protobuf (JSON only in the lab).
- No authentication, authorization, TLS, or network hardening.
- No multi-broker replication or partition-tuning beyond what the single-broker lab allows.
- No CDC / Debezium (explicit producer/consumer only).
- No production deployment, Kubernetes, or cloud provisioning.
- No persistence guarantees beyond what a single-node lab provides.

## Decisions

### D1. Kafka in KRaft mode, single broker
Use `confluentinc/cp-kafka` in KRaft (no Zookeeper). Rationale: Zookeeper is deprecated; KRaft reduces containers and RAM and reflects current/forward practice. Alternative considered: keeping Zookeeper (as both `eg-*` projects do) — rejected as legacy for a new lab. Single broker with topic replication factor 1; `order.created.v1` created at startup (init container running `kafka-topics --create`), default 3 partitions to allow parallelism experiments while keeping per-order ordering via key.

### D2. Two services, explicit producer/consumer
`order-service` (producer) and `logistic-service` (consumer), both Spring Boot with `spring-kafka`. Rationale: explicit `KafkaTemplate.send` / `@KafkaListener` is the clearest teaching model and mirrors `eg-ecommerce`. Alternative: CDC/outbox (YAS style) — rejected as too much machinery for the learning goal (noted as future evolution).

### D3. Event contract: JSON envelope + payload
Envelope fields: `eventId` (UUID), `eventType` (`order.created`), `eventVersion` (`1.0`), `occurredAt` (UTC ISO-8601), `source`, `traceId`; business data under `data`. Money in integer minor units (cents); message key = `orderId` for per-order ordering. Rationale: separates transport metadata from business data, enables idempotency and versioning, avoids float money errors. Alternative: flat payload — rejected (harder to evolve/route). Serialization: JSON via `JsonSerializer`/`JsonDeserializer` for `kafka-ui` readability; Avro + Schema Registry noted as future work.

### D4. Zero-code tracing via OTel Java Agent → Collector → Tempo
Attach `opentelemetry-javaagent.jar` to both services via `JAVA_TOOL_OPTIONS`/entrypoint; export OTLP (`:4317` gRPC) to an OpenTelemetry Collector that fans out traces to Tempo, metrics to Prometheus, logs to Loki (optional). Rationale: no telemetry code in services; the agent auto-instruments Spring Web and Spring Kafka and propagates `traceparent` in Kafka headers, yielding one trace across produce/consume. Alternative: Micrometer Tracing bridge in code — rejected for the lab (more code, same result). Critical detail: consumer-side context propagation requires container `observationEnabled(true)` when not relying solely on the agent; the lab will demonstrate the broken-trace case (Phase 6).

### D5. Metrics via Micrometer, Prometheus scrape 1s, kafka-exporter for lag
Services expose Micrometer metrics; counters `orders_produced_total` / `orders_consumed_total`, a processing-latency timer, plus JVM/Kafka client metrics. `kafka-exporter` provides consumer lag per partition. Prometheus scrapes at `1s`. Grafana dashboards show produced/s and consumed/s (via `rate()` over the counters), consumer lag, and latency percentiles. Rationale: application-level throughput answers "reads per second"; broker-level lag answers "is the consumer keeping up". Metrics can flow app→Collector→Prometheus (OTLP) and/or Prometheus scrape of `/actuator/prometheus`; the lab standardizes on the Collector path for consistency, with kafka-exporter scraped directly.

### D6. In-application rate limiter at 50 msg/s (per instance)
`logistic-service` wraps message handling with a rate limiter (e.g., Resilience4j `RateLimiter` or Guava `RateLimiter`) permitting 50 permits/second; the listener blocks/acquires before processing so the consumer naturally slows its poll cadence. Rationale: an explicit, deterministic "50" is easy to visualize and produces clean, predictable lag. Alternative: tuning `max.poll.records` / `max.poll.interval.ms` — rejected as harder to calibrate to an exact rate; documented as a complementary knob. Trade-off: the limit is per instance, so scaling to N replicas yields ~50×N aggregate; this is called out explicitly.

### D7. Load testing via JMeter over REST
A JMeter `.jmx` plan issues `POST /orders` at a configurable request rate (thread group + constant throughput timer) to exceed 50 msg/s and generate lag. Rationale: exercises the full path (HTTP → produce → consume) and is realistic. Alternative: JMeter Kafka plugins (pepper-box) producing directly to the broker — rejected as it bypasses the service and the produce-side trace; noted as optional.

### D8. Orchestration and layout
Single lab directory `shop-lab/` with: `order-service/`, `logistic-service/`, `docker-compose.yml` (infra + apps), and `observability/` (otel-collector, prometheus, tempo, grafana provisioning), plus `loadtest/` (JMeter plan). Rationale: one command to bring the lab up; mirrors the config-as-volume approach of the `eg-*` projects.

## Risks / Trade-offs

- [Trace breaks at the Kafka boundary if propagation is misconfigured] → Rely on the OTel Java Agent (auto-propagation) and enable `observationEnabled(true)`; intentionally demonstrate and then fix the broken case in Phase 6.
- [Single broker / RF=1 means no durability or failover] → Acceptable for a learning lab; explicitly a non-goal.
- [Rate limiter is per instance, so horizontal scaling changes aggregate throughput] → Documented; keep `logistic-service` at 1 replica for the baseline exercise.
- [1s Prometheus scrape increases cardinality/overhead] → Fine at lab scale; note it is not a production setting.
- [Consumer lag can grow unbounded under sustained overload, filling retention] → Bounded test durations and reasonable topic retention; reset via `docker compose down -v`.
- [JSON serialization has no enforced contract] → Accepted for the lab; Schema Registry/Avro listed as future evolution.
- [OTLP metrics via Collector vs direct Prometheus scrape can double-count if both enabled] → Standardize on one path (Collector) and scrape kafka-exporter directly only.

## Migration Plan

Greenfield addition; no migration. Rollout is `docker compose up` from `shop-lab/`. Teardown/rollback is `docker compose down -v` (removes volumes and state). No impact on existing `eg-*` projects or workspace tooling.

## Open Questions

- Partition count for `order.created.v1`: default to 3 (allows parallelism demos) or 1 (simplest ordering)? Leaning 3.
- Rate limiter library: Resilience4j vs Guava — either works; pick during implementation based on Spring Boot integration cleanliness.
- Logs pipeline (Loki) is marked optional; include in the first pass or defer until after traces + metrics are working?
