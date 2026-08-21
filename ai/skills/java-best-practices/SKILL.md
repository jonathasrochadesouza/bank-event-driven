---
name: java-best-practices
description: Apply and review modern Java 21, Spring Boot, Maven, JUnit Jupiter, and Mockito code using repository-aware conventions, official documentation, maintainable design, secure defaults, and proportionate verification. Use when implementing, refactoring, debugging, testing, or reviewing Java source files, Spring Boot APIs and configuration, Maven Java dependencies, or JUnit tests. Do not use for frontend-only, infrastructure-only, or non-Java work.
---

# Java Best Practices

Produce small, idiomatic, testable Java changes that respect the repository's actual toolchain. Treat compatibility and observable behavior as constraints, not assumptions.

## Required workflow

1. Read the nearest `AGENTS.md`, repository rules, build descriptor, and the Java files involved. Preserve established names and conventions unless the user requests a migration.
2. Determine the effective Java, Spring Boot, JUnit, Mockito, and build-tool versions from the build. Do not infer them from a documentation URL or upgrade a managed dependency in isolation.
3. Identify the change boundary and current behavior. Run the smallest useful baseline check when diagnosing a regression or making a risky refactor.
4. Load only the references needed for the task:
   - Read [java-language-and-style.md](references/java-language-and-style.md) for Java source, APIs, imports, naming, exceptions, collections, or concurrency.
   - Read [spring-boot.md](references/spring-boot.md) for controllers, services, configuration, persistence, validation, security, or application structure.
   - Read [testing.md](references/testing.md) for tests, JUnit, Mockito, test slices, or coverage decisions.
   - Read [review-checklist.md](references/review-checklist.md) for reviews or before completing a substantial implementation.
5. Explain material tradeoffs before changing public contracts, persistence, concurrency, security, or dependencies. Keep the patch focused and preserve unrelated user changes.
6. Implement the simplest design that expresses the domain. Prefer explicit dependencies, immutable values, cohesive methods, and framework defaults over custom machinery.
7. Add or update tests for observable behavior, edge cases, and failures. Avoid tests that merely mirror implementation details.
8. Run the narrowest relevant verification, inspect failures, and expand verification in proportion to risk. Report exactly what ran and what remains unverified.

## Decision rules

- Follow this priority: user request, repository instructions, build-enforced rules, version-matched official documentation, then this skill's defaults.
- Preserve behavior during a refactor unless a behavior change is explicit and tested.
- Prefer the versions managed by the Spring Boot parent or BOM. Override a managed version only for a documented compatibility or security reason and verify the complete dependency graph.
- Use stable Java 21 features when they improve clarity. Do not enable preview features without explicit approval and build configuration.
- Do not introduce a library for a small operation already covered safely by the JDK or Spring.
- Keep controllers concerned with HTTP translation, services with use cases, and repositories with persistence. Do not create layers that add no boundary or behavior.
- Use comments for rationale, constraints, and non-obvious consequences. Do not narrate straightforward code.
- Treat warnings, flaky behavior, disabled tests, swallowed exceptions, wildcard imports, and exposed secrets as defects to resolve or report.

## Repository profile

For this repository, preserve these defaults unless the user explicitly changes them:

- Use Java 21, Maven, Spring Boot 3.3.5, JUnit Jupiter, and Mockito as resolved by `backend/pom.xml`.
- Preserve the spelling `shortner`, the package `com.jkrocha.shortner`, the artifact `url-shortner-backend`, and the DynamoDB table `url-shortner`.
- Do not silently migrate to the newest Spring Boot or JUnit documentation version. Check compatibility with the Spring Boot dependency management first.
- Verify backend logic with `cd backend && mvn test`; use `mvn clean package` only when packaging or integration risk warrants it.
- Follow `ai/instructions/repository-rules.md`, including its schema and Terraform constraints.

## Delivery standard

Conclude with the behavior changed, important design choices, files affected, verification performed, and any concrete residual risk. During a review, lead with actionable findings ordered by severity and include precise file locations.
