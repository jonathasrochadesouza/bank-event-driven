# Java review checklist

Use this checklist proportionately. Report only concrete issues introduced by or relevant to the reviewed change.

## Correctness and contracts

- Does the implementation satisfy the requested behavior, including absence, invalid input, boundaries, and failure paths?
- Are public API, serialization, persistence, and redirect contracts preserved or intentionally migrated?
- Are equality, ordering, uniqueness, numeric overflow, locale, encoding, time zone, and `null` semantics explicit where relevant?
- Can a retry, race, collision, duplicate request, or partial failure corrupt state or return the wrong result?

## Design and maintainability

- Are responsibilities cohesive and dependencies explicit?
- Is there avoidable mutable or global state, temporal coupling, duplication, or framework leakage?
- Does a new abstraction remove real complexity, or only add indirection?
- Are records, sealed types, pattern matching, streams, and `var` used for clarity rather than novelty?
- Do names and comments explain domain intent and rationale?

## Spring and data

- Does configuration fail fast, remain externalized, and avoid secrets?
- Are controllers thin, validation layered correctly, and errors stable and safe?
- Are managed dependency versions preserved unless an override is justified and verified?
- Are database/SDK calls bounded, paginated when necessary, and equipped with deliberate timeout/retry behavior?
- Are resource lifecycles and Lambda reuse safe?

## Security and operations

- Are authentication, authorization, CORS, redirects, URL parsing, and untrusted input handled explicitly?
- Could logs, exceptions, or responses expose secrets or sensitive data?
- Are randomness and token generation appropriate, with collision and exhaustion behavior defined?
- Is operational logging useful without duplication or excessive volume?

## Tests and verification

- Do tests exercise behavior rather than implementation details?
- Are success, edge, and failure cases deterministic and isolated?
- Is the chosen test level appropriate: unit, slice, integration, or packaging?
- Were repository-prescribed commands run, and are skipped or unverified areas disclosed?

## Review output

For each defect, state severity, exact location, triggering scenario, impact, and a focused correction. Do not present style preferences as correctness defects when the repository has no such rule. If no actionable issue remains, say so and mention residual testing or compatibility risks.
