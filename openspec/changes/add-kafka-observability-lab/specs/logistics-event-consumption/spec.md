## ADDED Requirements

### Requirement: Consume order.created.v1 events
`logistic-service` SHALL consume `order.created.v1` events using a Kafka listener bound to a dedicated consumer group and process each event's business payload.

#### Scenario: Event is consumed and processed
- **WHEN** an `order.created.v1` event is available on the topic
- **THEN** `logistic-service` consumes it and processes the order payload

#### Scenario: Consumer group is registered
- **WHEN** `logistic-service` is running
- **THEN** its consumer group is registered on the broker and its offsets advance as events are processed

### Requirement: Rate limiting at 50 messages per second
`logistic-service` SHALL cap processing at 50 messages per second per instance using an in-application rate limiter, acquiring a permit before processing each message.

#### Scenario: Throughput is capped under overload
- **WHEN** events arrive faster than 50 per second
- **THEN** the instance processes at most approximately 50 messages per second
- **AND** unprocessed events accumulate as consumer lag rather than being dropped

#### Scenario: No artificial delay under light load
- **WHEN** events arrive slower than 50 per second
- **THEN** each event is processed without rate-limiter-induced delay

### Requirement: Idempotent handling by eventId
Because delivery is at-least-once, `logistic-service` SHALL deduplicate by `eventId` so that reprocessing the same event does not produce duplicate side effects.

#### Scenario: Duplicate event is ignored
- **WHEN** an event with an `eventId` that was already processed is delivered again
- **THEN** the service recognizes it as a duplicate and does not repeat the side effect

### Requirement: Consumed-message counter
`logistic-service` SHALL increment a Micrometer counter for every successfully processed event so consumption throughput can be derived.

#### Scenario: Counter increments on processing
- **WHEN** an event is successfully processed
- **THEN** the consumed-message counter increases by one
