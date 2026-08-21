# Verification and Review

Use this reference before completing a Terraform implementation or when reviewing Terraform code and plans.

## Verification ladder

Run the narrowest applicable checks and expand with risk:

1. `terraform fmt -check -recursive` for read-only formatting verification, or `terraform fmt` when authorized to format the changed module.
2. `terraform init -backend=false` when validation needs providers or modules but backend access is unnecessary. Do not use `-upgrade` unless updating dependencies intentionally.
3. `terraform validate` for syntax, internal consistency, argument names, and types. It does not validate credentials, remote APIs, existing state, or all provider semantics.
4. Existing repository linters, security scanners, documentation checks, and policy tests. Do not introduce or install new tools silently.
5. `terraform test` for native module tests. Inspect each test first: apply-mode tests can create billable infrastructure and require explicit authorization.
6. A speculative `terraform plan` against the correct environment for provider and state-aware verification. This requires authorized credentials and may refresh remote objects.
7. An approved saved plan and apply only when the user explicitly requested infrastructure mutation.

If Terraform is unavailable, report that limitation instead of claiming validation. Do not substitute an HCL parser for provider-aware validation without stating the reduced coverage.

## Test meaningful behavior

- Use variable validation, preconditions, postconditions, and check blocks for invariants close to the configuration.
- Use plan-mode native tests for input validation, resource attributes, module wiring, outputs, and expected failures when no real infrastructure is needed.
- Use mocked providers when the behavior under test does not require the provider API and the repository's Terraform version supports the needed mocking feature.
- Reserve apply-mode integration tests for provider behavior that cannot be proven statically. Isolate accounts or projects, constrain cost and permissions, use unique names, and guarantee cleanup reporting.
- Test module contracts and observable plans rather than copying implementation expressions into assertions.

## Review code and configuration

Check:

- version constraints and lock-file changes are intentional
- provider configuration stays in the root and aliases are passed explicitly
- resource and module addresses remain stable or have reviewed migration blocks
- inputs are typed and documented; validations represent real constraints
- outputs are necessary, documented, and do not expose secrets unnecessarily
- direct references express dependencies; `depends_on` is narrow and justified
- `count` or `for_each` keys remain stable across reorderings and unknown values
- lifecycle rules have documented operational consequences
- generated names meet provider limits and preserve established identifiers
- IAM is least-privilege; storage and transport encryption remain enabled
- no credentials, secrets, state, plans, or private material enter the diff
- comments explain why rather than narrating what
- unrelated churn and generated-file edits are absent

## Review the plan

Check:

- the account, region, workspace or environment, backend, and variables are correct
- action counts match the request
- replacements and destroys are understood and explicitly acceptable
- stateful resources have backup, retention, deletion protection, and migration considerations
- network ingress, public endpoints, IAM actions and resources, encryption, and logging are safe
- unknown values do not conceal an unsafe address, policy, or replacement
- data sources and refresh did not reveal unexpected drift
- provider upgrades did not cause unrequested normalization or replacement
- estimated operational cost and quota impact are proportionate

## Delivery checklist

- State the behavior and files changed.
- State whether resource addresses, state, providers, backend, or module interfaces changed.
- State plan action counts and destructive or replacement effects when a plan ran.
- State security, availability, and cost implications.
- List exact verification commands and results.
- Identify missing credentials, unavailable tools, skipped integration tests, or other residual risk.

## Source basis

The verification flow follows the guide's [formatting and pre-commit recommendations](https://www.terraform-best-practices.com/code-styling) and HashiCorp's current documentation for [`validate`](https://developer.hashicorp.com/terraform/cli/commands/validate), [`test`](https://developer.hashicorp.com/terraform/cli/commands/test), and [`plan`](https://developer.hashicorp.com/terraform/cli/commands/plan).
