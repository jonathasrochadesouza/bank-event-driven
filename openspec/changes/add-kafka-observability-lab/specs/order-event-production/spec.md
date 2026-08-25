## ADDED Requirements

### Requirement: Order intake endpoint
`order-service` SHALL expose `POST /orders` that accepts an order request and returns a success response containing the generated `orderId`.

#### Scenario: Valid order request is accepted
- **WHEN** a client sends a valid `POST /orders` request
- **THEN** the service responds with a 2xx status and a body containing the `orderId`

#### Scenario: Invalid order request is rejected
- **WHEN** a client sends a request missing required fields
- **THEN** the service responds with a 4xx status and does not publish an event

### Requirement: Publish order.created.v1 event
On accepting a valid order, `order-service` SHALL publish exactly one `order.created.v1` event to Kafka, keyed by `orderId`, using the JSON envelope contract.

#### Scenario: Event published on order creation
- **WHEN** a valid order is accepted
- **THEN** one record is produced to `order.created.v1` with key equal to the `orderId`
- **AND** the record value conforms to the event contract

#### Scenario: Envelope fields are populated
- **WHEN** an event is produced
- **THEN** `eventId` is a unique UUID, `eventType` is `order.created`, `eventVersion` is `1.0`, `occurredAt` is a UTC ISO-8601 timestamp, and `source` identifies `order-service`

### Requirement: Produced-message counter
`order-service` SHALL increment a Micrometer counter for every successfully produced event so throughput can be derived.

#### Scenario: Counter increments on publish
- **WHEN** an event is successfully produced
- **THEN** the produced-message counter increases by one
