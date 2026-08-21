# Spring Boot practices

Apply these rules against the project's effective Spring Boot version. Use version-matched reference documentation for exact annotations, defaults, and compatibility.

## Structure and dependency injection

- Place the application class in a root package so component scanning has an intentional boundary.
- Use constructor injection for required collaborators. Omit `@Autowired` on a single constructor.
- Keep injected fields `private final`. Avoid field injection, service locators, and hidden global state.
- Create a bean only when lifecycle, configuration, replacement, or cross-cutting behavior benefits from container management.
- Keep controllers thin, services centered on use cases, and repositories centered on persistence. Keep domain rules independent of HTTP and AWS SDK details where practical.
- Prevent circular dependencies by correcting responsibilities rather than enabling circular references or adding lazy injection as a default fix.

## Configuration

- Prefer typed `@ConfigurationProperties` over scattered `@Value` expressions. Use records when binding and validation requirements support them.
- Validate required configuration at startup with Bean Validation. Supply defaults only when they are safe in every target environment.
- Keep configuration externalized. Never commit secrets or put credentials in logs, exception messages, defaults, or test fixtures.
- Let Spring Boot's parent/BOM manage supported dependency versions. Inspect `mvn dependency:tree` before overriding or diagnosing version conflicts.
- Separate environment differences with properties and deployment configuration, not conditional business logic.

## HTTP APIs and validation

- Model request and response contracts explicitly. Do not expose persistence entities as the external API by convenience.
- Validate syntax at the transport boundary and enforce domain invariants in the domain/service boundary.
- Use correct status codes, headers, media types, and redirect semantics. Keep error responses stable and machine-readable.
- Centralize shared exception-to-response translation with `@RestControllerAdvice`; keep local handlers only when behavior is truly controller-specific.
- Do not leak stack traces, implementation details, database keys, or sensitive values to clients.
- Treat CORS, authentication, authorization, input size, redirect targets, and URL handling as security decisions. Avoid wildcard origins outside an explicitly public use case.

## Persistence and external systems

- Express repository absence with `Optional` when appropriate; do not call `Optional.get()` without a proven presence.
- Keep persistence identifiers and serialized attribute names stable. Plan migrations explicitly before renaming them.
- Define timeout, retry, idempotency, and partial-failure behavior for remote systems. Avoid unbounded retries and hot loops.
- Do not load or scan unbounded datasets for endpoints expected to scale. Add pagination only as part of an explicit API and persistence design change.
- Map SDK exceptions at the boundary where actionable context can be added; preserve the cause and avoid duplicate logging.

## Operations and serverless

- Use Actuator, metrics, and health checks only when deployment and exposure are intentionally configured. Never expose sensitive actuator endpoints publicly.
- Log request correlation identifiers when available and prefer meaningful operational events over noisy per-line logs.
- For Lambda, keep initialization deterministic, reuse thread-safe clients across invocations, and avoid work in cold-start paths that can be deferred safely.
- Close application-owned clients during lifecycle shutdown when the runtime requires it; do not close shared clients per request.
- Measure before optimizing startup, allocation, or reflection. Preserve correctness and diagnostics when tuning native/serverless behavior.

## Primary sources

- [Spring Boot documentation index](https://docs.spring.io/spring-boot/index.html)
- [Spring Boot 3.3 reference](https://docs.spring.io/spring-boot/3.3/index.html)
- [Spring Boot testing](https://docs.spring.io/spring-boot/3.3/reference/testing/index.html)
- [Spring Boot externalized configuration](https://docs.spring.io/spring-boot/3.3/reference/features/external-config.html)
- [Spring Boot dependency versions](https://docs.spring.io/spring-boot/3.3/appendix/dependency-versions/coordinates.html)
