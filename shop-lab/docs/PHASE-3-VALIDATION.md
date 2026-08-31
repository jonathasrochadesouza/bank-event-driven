# Phase 3 validation — distributed tracing

## What is wired

- OpenTelemetry Java Agent `2.31.1` is mounted read-only in both services.
- Each service exports OTLP/gRPC to `otel-collector:4317` with an explicit
  service name.
- The Collector receives OTLP, batches traces, and exports them to Tempo.
- Tempo is reachable on http://localhost:3200. Its Grafana data-source
  provisioning file is ready for the Grafana service added in Phase 4.
- `spring.kafka.listener.observation-enabled` is enabled so listener processing
  continues the Kafka trace context.

## Run and verify

```bash
cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab
docker compose up -d --build

curl -sS -X POST http://localhost:18080/orders \
  -H 'Content-Type: application/json' \
  --data @scripts/sample-order.json

# List recent traces and select the entry whose root span is POST /orders.
curl -sS 'http://localhost:3200/api/search?limit=100'

# Replace TRACE_ID with that trace ID. The result must contain spans from both
# services: POST /orders, order.created.v1 publish, and order.created.v1 process.
curl -sS http://localhost:3200/api/traces/TRACE_ID
```

The same trace is available through the provisioned Tempo data source once
Grafana is introduced in Phase 4.
