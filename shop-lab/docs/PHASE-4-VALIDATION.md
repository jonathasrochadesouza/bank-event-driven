# Phase 4 validation — throughput metrics and dashboard

## Services

| Service | Host URL | Purpose |
| --- | --- | --- |
| Prometheus | http://localhost:9091 | Scrapes metrics every second. |
| Grafana | http://localhost:3001 | Dashboard and trace exploration. Sign in with `admin` / `admin`. |
| kafka-exporter | internal only (`kafka-exporter:9308`) | Exposes consumer-group lag per partition. |

## Prometheus targets

Prometheus uses a `1s` scrape interval and must show these targets as `up` at
http://localhost:9091/targets:

- `otel-collector`
- `kafka-exporter`
- `order-service`
- `logistic-service`

The services are scraped in addition to the Collector because the business
Micrometer counters and timer are exposed by their Actuator Prometheus
endpoints. The Collector target supplies Collector and agent telemetry.

## Dashboard

Grafana provisions **Shop Lab / Shop Lab - Throughput and Lag** with these
panels:

1. Orders produced per second
2. Orders consumed per second
3. Consumer lag for the `logistic-service` group
4. Processing latency p50, p95, and p99

## Quick verification

```bash
cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab
docker compose up -d

curl -sS -X POST http://localhost:18080/orders \
  -H 'Content-Type: application/json' \
  --data @scripts/sample-order.json

# All four jobs should be up.
curl -sS http://localhost:9091/api/v1/targets

# The dashboard's core series should each return data.
curl -sSG http://localhost:9091/api/v1/query \
  --data-urlencode 'query=orders_produced_total'
curl -sSG http://localhost:9091/api/v1/query \
  --data-urlencode 'query=orders_consumed_total'
curl -sSG http://localhost:9091/api/v1/query \
  --data-urlencode 'query=kafka_consumergroup_lag{consumergroup="logistic-service",topic="order.created.v1"}'
```
