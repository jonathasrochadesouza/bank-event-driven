## ADDED Requirements

### Requirement: Zero-code instrumentation via OpenTelemetry Java Agent
Both `order-service` and `logistic-service` SHALL be instrumented using the OpenTelemetry Java Agent attached at startup, without adding tracing code to the services.

#### Scenario: Services start with the agent attached
- **WHEN** either service starts
- **THEN** the OpenTelemetry Java Agent is active and emits telemetry via OTLP
- **AND** no application source code contains manual span creation for the request/produce/consume path

### Requirement: End-to-end trace across Kafka
A single trace SHALL span the full path from `POST /orders` through the Kafka produce and the Kafka consume to the processing in `logistic-service`, by propagating trace context in Kafka message headers.

#### Scenario: One trace covers HTTP, produce, and consume
- **WHEN** an order is created and subsequently consumed
- **THEN** the resulting trace in Tempo contains spans for the HTTP request, the Kafka produce, the Kafka consume, and the processing
- **AND** all these spans share the same trace id

#### Scenario: Trace context travels in Kafka headers
- **WHEN** an event is produced
- **THEN** the Kafka record carries trace-context headers (e.g., `traceparent`)
- **AND** the consumer continues the same trace rather than starting a new one

### Requirement: Trace delivery to Tempo via Collector
Traces SHALL be exported over OTLP to an OpenTelemetry Collector, which forwards them to Tempo, and Tempo SHALL be queryable as a Grafana data source.

#### Scenario: Trace is visible in Grafana
- **WHEN** a user searches recent traces in Grafana using the Tempo data source
- **THEN** the end-to-end order trace is found and its spans can be inspected
