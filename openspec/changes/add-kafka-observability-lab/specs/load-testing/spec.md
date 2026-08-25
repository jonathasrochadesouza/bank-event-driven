## ADDED Requirements

### Requirement: JMeter REST load plan
The lab SHALL provide a JMeter test plan that generates load by sending `POST /orders` requests to `order-service` at a configurable request rate.

#### Scenario: Load plan drives orders
- **WHEN** the JMeter plan is executed against `order-service`
- **THEN** it issues `POST /orders` requests and receives successful responses

#### Scenario: Request rate is configurable
- **WHEN** the target rate is changed via the plan's parameters
- **THEN** the effective request rate against `order-service` changes accordingly

### Requirement: Load exceeds consumer capacity to demonstrate lag
The JMeter plan SHALL be able to sustain a request rate above 50 messages per second so that consumer lag becomes observable in the dashboards.

#### Scenario: Overload produces observable lag
- **WHEN** the plan runs at a rate above 50 requests per second for a sustained period
- **THEN** produced/s exceeds consumed/s in Grafana
- **AND** the consumer lag metric increases during the test
