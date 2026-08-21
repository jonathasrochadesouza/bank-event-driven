# Java 21 language and style

Use these defaults when repository-specific formatting or static-analysis rules do not say otherwise.

## Source structure and imports

- Declare a package in every ordinary source file and keep one top-level class per file.
- Never use wildcard imports, including static wildcards.
- Keep imports on one line. Put static imports in one group, then one blank line, then non-static imports. Sort imported names in ASCII order within each group.
- Import static nested classes normally, not with a static import.
- Keep overloads and constructors with the same name contiguous. Organize members by a maintainable logical order.
- Use braces consistently and make control flow visually unambiguous. Prefer small methods over deeply nested blocks.
- Target a 100-column limit when practical; package declarations, imports, long URLs, command examples, and text-block contents are reasonable exceptions.

## Names and APIs

- Choose names that expose domain meaning. Use nouns for types and values, verbs for actions, and `is`/`has`/`can` for booleans when natural.
- Avoid abbreviations that are not established domain vocabulary. Preserve stable public and persistence names.
- Keep APIs small. Minimize visibility and mutability; expose interfaces only where they express a real boundary.
- Validate invariants at the boundary that owns them. Reject invalid state early with a useful message.
- Do not use `null` to encode several meanings. Use an empty collection for "no elements" and `Optional<T>` primarily for a possibly absent return value. Avoid `Optional` fields and parameters unless a framework or established API requires them.

## Java 21 choices

- Prefer records for transparent immutable data carriers when framework requirements permit them. Validate record components in a compact constructor when the record owns an invariant.
- Use sealed hierarchies when the set of variants is intentionally closed and exhaustive handling provides value.
- Use pattern matching for `instanceof`, record patterns, and `switch` when it removes casting or branching noise and remains clearer than polymorphism.
- Use `var` only for local variables when the initializer makes the type obvious; keep explicit types when they convey important domain or API information.
- Prefer switch expressions for value selection. Make exhaustiveness intentional and handle `null` explicitly when it is a valid input.
- Avoid preview features unless the build explicitly enables them and the user accepts the operational cost.

## State, collections, and resources

- Prefer immutable objects and final fields. Make defensive copies at mutable collection, array, date, and buffer boundaries.
- Program to the narrowest useful collection interface. Preserve ordering and uniqueness only when the contract requires them.
- Avoid side effects inside stream pipelines. Use a loop when it communicates mutation, early exit, checked failure, or complex control flow better.
- Close `AutoCloseable` resources with try-with-resources. Do not rely on finalization.
- Use `java.time` types for time and inject a `Clock` when behavior depends on the current instant.

## Errors, logging, and security

- Throw the most specific meaningful exception. Preserve the original cause when translating failures.
- Do not catch an exception only to ignore it, log it twice, or throw a context-free `RuntimeException`.
- Use exceptions for exceptional outcomes, not ordinary branching. Keep error messages useful without exposing credentials or sensitive data.
- Use structured, parameterized logging. Do not use `System.out`, log secrets, or concatenate expensive log messages eagerly.
- Use `SecureRandom` for security-sensitive or unpredictable tokens. Define collision, retry, and exhaustion behavior explicitly.
- Avoid shared mutable state. When concurrency is required, document ownership and thread-safety and prefer high-level concurrency utilities over manual locking.

## Primary sources

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Java SE 21 documentation](https://docs.oracle.com/en/java/javase/21/)
- [Java SE 21 language changes](https://docs.oracle.com/en/java/javase/21/language/java-language-changes-release.html)
- [Record classes](https://docs.oracle.com/en/java/javase/21/language/records.html)
- [Pattern matching](https://docs.oracle.com/en/java/javase/21/language/pattern-matching.html)
