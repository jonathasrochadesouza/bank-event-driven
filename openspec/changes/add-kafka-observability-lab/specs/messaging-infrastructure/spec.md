## ADDED Requirements

### Requirement: Kafka broker in KRaft mode
The lab SHALL provide a single Apache Kafka broker running in KRaft mode (without Zookeeper), orchestrated via Docker Compose and reachable by both services on the lab network.

#### Scenario: Broker starts without Zookeeper
- **WHEN** the lab stack is started with `docker compose up`
- **THEN** a single Kafka broker container becomes healthy in KRaft mode
- **AND** no Zookeeper container is present in the stack

#### Scenario: Services connect to the broker
- **WHEN** `order-service` and `logistic-service` start
- **THEN** each establishes a connection to the broker using the configured bootstrap servers

### Requirement: Topic provisioning
The lab SHALL ensure the topic `order.created.v1` exists before message traffic begins, created automatically at stack startup.

#### Scenario: Topic exists at startup
- **WHEN** the lab stack finishes starting
- **THEN** the topic `order.created.v1` exists on the broker
- **AND** its partition count matches the configured value

#### Scenario: Idempotent topic creation
- **WHEN** the stack is restarted and the topic already exists
- **THEN** startup completes without error and does not duplicate or recreate the topic

### Requirement: Event contract (JSON envelope and payload)
Events published to `order.created.v1` SHALL use a JSON structure with a transport envelope and a business payload. The envelope MUST include `eventId` (UUID), `eventType`, `eventVersion`, `occurredAt` (UTC ISO-8601), and `source`. The payload MUST include order details with monetary amounts expressed in integer minor units (cents) and timestamps in UTC ISO-8601. The message key MUST be `orderId`.

#### Scenario: Well-formed event is published
- **WHEN** an order is created
- **THEN** the published record has key equal to the `orderId`
- **AND** the value is JSON containing the envelope fields `eventId`, `eventType`, `eventVersion`, `occurredAt`, `source` and a `data` payload
- **AND** monetary amounts in the payload are integers in minor units

#### Scenario: Event is human-readable in kafka-ui
- **WHEN** a user inspects the topic in kafka-ui
- **THEN** the message value is displayed as readable JSON

### Requirement: Topic inspection UI
The lab SHALL provide `kafka-ui` connected to the broker for inspecting topics, partitions, consumer groups, and messages.

#### Scenario: kafka-ui shows the topic and consumer group
- **WHEN** a user opens kafka-ui after traffic has flowed
- **THEN** the topic `order.created.v1` and the `logistic-service` consumer group are visible with their offsets and lag
