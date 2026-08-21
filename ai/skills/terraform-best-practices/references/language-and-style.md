# Language and Style

Use this reference when writing or reviewing HCL configuration.

## Format and organize

- Let `terraform fmt` define whitespace and alignment. Do not hand-format against its canonical output.
- Use lowercase identifiers with underscores. Use descriptive singular nouns for singleton resource and data names; do not repeat the resource type in the local name.
- Use `#` comments for rationale, constraints, operational warnings, and non-obvious provider behavior. Remove comments that merely restate the code.
- Place dependent resources after the resources they reference when practical. This does not create dependencies, but makes the graph easier to read.
- Keep arguments together, then nested blocks, then meta-arguments such as `depends_on` and `lifecycle`. Let established repository style win when it is already consistent.

## Express dependencies and repetition

- Prefer direct attribute references; Terraform derives graph edges from expressions.
- Avoid explicit `depends_on` when a reference can express the same dependency. Broad dependencies create conservative and noisy plans.
- Prefer `for_each` when instances have stable semantic keys. Prefer `count` for homogeneous positional instances or a simple optional singleton.
- Never derive `for_each` keys from values unknown until apply. Avoid converting unstable lists to sets when address churn would be costly.
- Use `dynamic` blocks only when a real nested-block collection varies. A literal nested block is clearer for fixed configuration.
- Use `locals` for repeated expressions or meaningful derived values, not as an alias layer for every variable.
- Prefer built-in functions and provider resources over shell provisioners. Treat `local-exec` and `remote-exec` as last resorts with explicit failure and idempotency reasoning.

## Design inputs

- Give every variable a concrete type and description. Use `any` only when arbitrary shape is an intentional contract.
- Order variable attributes consistently: `description`, `type`, `default`, `sensitive`, `nullable`, then validation blocks, unless repository tooling enforces another order.
- Use plural names for collections and positive booleans such as `encryption_enabled`.
- Add defaults only for optional values with safe, unsurprising behavior. Set `nullable = false` when `null` has no valid semantic meaning.
- Use object types and optional attributes when callers benefit from a stable schema. Avoid giant objects that couple unrelated settings.
- Add validation for domain constraints Terraform or the provider cannot express early. Write an actionable error message and do not duplicate simple type checks.
- Do not pass secrets through `.tfvars` committed to version control. Mark secret inputs `sensitive`, while remembering they may still enter state.

## Design outputs

- Name outputs for the concept and attribute they expose; use plural names for collections.
- Give every output a description and expose only values needed by operators, automation, or other configurations.
- Mark confidential outputs `sensitive` to redact normal CLI display. Do not mistake redaction for state encryption or omission.
- Prefer empty collections or `null` with a documented contract over sentinel strings such as `""`.
- Avoid legacy splat and `element(concat(...))` workarounds when modern expressions, `try`, conditionals, or `one` communicate intent directly.

## Manage lifecycle safely

- Use `create_before_destroy` only after confirming name uniqueness, quotas, and provider behavior.
- Use `prevent_destroy` for exceptional high-value resources, not as a substitute for review and backups.
- Use `ignore_changes` only for attributes intentionally owned elsewhere. Document the external owner and recognize that ignored drift becomes invisible to normal reconciliation.
- Add `precondition`, `postcondition`, and `check` blocks for important invariants when they provide clearer failures than provider errors.

## Source basis

These conventions synthesize the guide's [naming](https://www.terraform-best-practices.com/naming) and [code styling](https://www.terraform-best-practices.com/code-styling) sections with HashiCorp's current [Terraform style guide](https://developer.hashicorp.com/terraform/language/style).
