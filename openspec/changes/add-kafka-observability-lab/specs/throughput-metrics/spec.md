## ADDED Requirements

### Requirement: Application metrics via Micrometer
Both services SHALL expose Micrometer metrics including produced/consumed counters and a processing-latency timer, delivered to Prometheus so per-second rates can be computed.

#### Scenario: Rates are derivable from counters
- **WHEN** traffic flows through the lab
- **THEN** Prometheus stores the produced and consumed counters
- **AND** a per-second rate can be computed for each using a range function

#### Scenario: Processing latency is recorded
- **WHEN** `logistic-service` processes events
- **THEN** a latency timer records processing durations from which percentiles can be derived

### Requirement: Prometheus scrape at 1 second
Prometheus SHALL scrape metrics targets at a 1-second interval to provide near-real-time dashboards.

#### Scenario: Scrape interval is 1s
- **WHEN** Prometheus configuration is loaded
- **THEN** the effective scrape interval for the lab targets is 1 second

### Requirement: Consumer lag metric via kafka-exporter
The lab SHALL run kafka-exporter and expose consumer lag per partition for the `logistic-service` consumer group to Prometheus.

#### Scenario: Lag rises under overload
- **WHEN** the producer sustains a rate above 50 messages per second
- **THEN** the consumer lag metric for the `logistic-service` group increases over time

#### Scenario: Lag recovers when load drops
- **WHEN** the incoming rate falls below 50 messages per second
- **THEN** the consumer lag metric decreases back toward zero

### Requirement: Grafana dashboards
Grafana SHALL provide dashboards showing produced per second, consumed per second, consumer lag, and processing latency, backed by Prometheus.

#### Scenario: Throughput and lag are visualized
- **WHEN** a user opens the lab dashboard during a load test
- **THEN** produced/s, consumed/s, consumer lag, and latency are displayed and update in near real time
