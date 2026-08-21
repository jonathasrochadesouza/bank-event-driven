# JUnit Jupiter and Mockito practices

Test observable contracts with the cheapest test that gives trustworthy evidence. Resolve the JUnit version from the build and its dependency management before using version-specific APIs.

## Test design

- Give each test one behavioral reason to fail. Use Arrange-Act-Assert or Given-When-Then consistently without ceremonial comments.
- Name tests after behavior and condition, using the repository's language and convention. Keep failure output easy to understand.
- Cover the happy path, meaningful boundaries, invalid input, absence, exceptions, and regression scenarios. Do not chase a coverage percentage with assertions that prove nothing.
- Keep tests deterministic and isolated from wall-clock time, random values, execution order, network access, and shared mutable state. Inject `Clock`, random generators, clients, or gateways when control matters.
- Prefer real values and small fakes for simple collaborators. Mock process boundaries or expensive dependencies, not value objects or the class under test.
- Assert returned values, state transitions, persisted commands, and externally relevant interactions. Avoid overspecifying call order and incidental internal calls.

## JUnit Jupiter

- Use `org.junit.jupiter` APIs. Do not add JUnit Vintage for new tests.
- Keep test classes and methods package-private unless cross-package inheritance or the module path requires `public`; never make lifecycle or test methods `private`.
- Use `@ParameterizedTest` for the same rule across a meaningful input table. Use separate tests when cases represent different behaviors or deserve different failure messages.
- Use `assertAll` for independent properties of one result and `assertThrows` for both exception type and meaningful details when the message or cause is part of the contract.
- Use lifecycle methods only for shared setup that improves clarity. Avoid large mutable fixtures and inheritance-heavy test hierarchies.
- Do not disable tests as a workaround. If temporary disabling is unavoidable, provide a reason and traceable issue, as recommended by JUnit.
- Do not impose test ordering to hide state coupling. Fix the isolation problem.

## Mockito and Spring tests

- Use `@ExtendWith(MockitoExtension.class)` for Mockito unit tests and strict stubbing defaults. Remove unused stubs.
- Stub only behavior used by the scenario. Prefer exact arguments; use broad matchers such as `any()` only when the argument is irrelevant to the contract.
- Verify interactions only when the interaction itself is observable behavior, such as not writing after failed validation.
- Use plain unit tests for domain and service logic. Use focused Spring test slices for MVC, JSON, persistence, or configuration boundaries.
- Use `@SpringBootTest` only when application wiring or end-to-end Spring behavior is the subject. Do not start the full context for every test.
- For HTTP controllers, assert status, headers, body, validation, and exception mapping. For repositories, test real serialization/key mappings against a representative test environment when feasible.

## Version compatibility

- JUnit 6.1.3 requires Java 17 or newer, so Java 21 satisfies its runtime floor; that fact alone does not make it compatible with the project's Spring Boot line and plugins.
- When Spring Boot manages test dependencies, prefer `spring-boot-starter-test` and its curated versions. Do not pin a JUnit component independently without checking the Platform/Jupiter versions, Surefire support, and Spring Test compatibility.
- Treat examples from newer JUnit documentation as conceptual until the used API exists in the effective dependency version.

## Verification

- Run the narrowest test class or package while iterating, then the repository-prescribed suite before completion.
- Inspect the first causal failure rather than repeatedly rerunning a failing suite.
- Report skipped tests, environment-dependent omissions, and tests that could not run.

## Primary sources

- [JUnit 6.1.3 overview](https://docs.junit.org/6.1.3/overview.html)
- [JUnit writing tests](https://docs.junit.org/6.1.3/writing-tests/intro.html)
- [JUnit test classes and methods](https://docs.junit.org/6.1.3/writing-tests/test-classes-and-methods.html)
- [JUnit disabling tests](https://docs.junit.org/6.1.3/writing-tests/disabling-tests.html)
- [Spring Boot 3.3 testing](https://docs.spring.io/spring-boot/3.3/reference/testing/index.html)
