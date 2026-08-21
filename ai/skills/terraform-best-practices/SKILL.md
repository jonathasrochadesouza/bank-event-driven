---
name: terraform-best-practices
description: Apply and review safe, maintainable Terraform infrastructure using repository-aware structure, explicit state boundaries, stable module interfaces, secure state and secret handling, version constraints, canonical formatting, validation, plans, and proportionate tests. Use when implementing, refactoring, debugging, testing, or reviewing Terraform or HCL files, modules, providers, backends, state migrations, imports, CI checks, or infrastructure plans. Do not use for application-only changes or for operating cloud resources outside Terraform.
---

# Terraform Best Practices

Produce small, readable, reviewable Terraform changes with controlled state impact. Treat the execution plan, state boundary, provider schema, and repository conventions as part of the design.

## Required workflow

1. Read the nearest `AGENTS.md`, repository rules, relevant `.tf` and `.tfvars` files, `.terraform.lock.hcl`, tests, CI configuration, and module documentation. Preserve existing names and state addresses unless migration is explicit.
2. Determine the effective Terraform and provider versions from configuration and the lock file. Check provider documentation matching those constraints before using an argument or resource feature.
3. Classify the change as configuration-only, dependency update, state migration, backend change, module-interface change, or infrastructure operation. Identify the affected root module, workspace or environment, state boundary, credentials, and blast radius.
4. Load only the references needed for the task:
   - Read [architecture-and-modules.md](references/architecture-and-modules.md) for directory structure, state boundaries, compositions, modules, providers, dependencies, and versioning.
   - Read [language-and-style.md](references/language-and-style.md) for HCL layout, naming, variables, locals, outputs, meta-arguments, conditions, and documentation.
   - Read [state-security-and-operations.md](references/state-security-and-operations.md) for backends, state, secrets, IAM, plans, applies, imports, moves, removals, drift, and destructive changes.
   - Read [verification-and-review.md](references/verification-and-review.md) for implementation verification, native tests, CI, reviews, and the completion checklist.
5. Inspect the current resource graph and infer dependencies from references. Explain material tradeoffs before changing a public module interface, provider or Terraform version, backend, state address, replacement behavior, security boundary, or cost profile.
6. Implement the smallest cohesive change. Prefer direct references, provider-native resources and data sources, typed inputs, explicit outputs, and simple modules over indirection or premature abstraction.
7. Format changed Terraform files and run the narrowest safe verification. Inspect the plan whenever credentials and a configured backend are available and the user authorized interaction with the target environment.
8. Review the final diff and any plan for replacements, destroys, privilege expansion, public exposure, secret leakage, unexpected dependencies, and unrelated changes. Report commands run and anything not verified.

## Decision rules

- Follow this priority: user request, repository instructions, version-matched provider and Terraform documentation, established project conventions, then this skill's defaults.
- Distinguish a reusable resource module from a root composition. Configure providers and backends in root modules; pass dependencies and provider aliases explicitly to child modules.
- Start simple. Split configurations or state only around ownership, lifecycle, security, environment, region, account, deployment frequency, or blast-radius boundaries—not merely file length.
- Keep resource addresses stable. Use `moved` blocks for refactors when supported; plan imports and removals explicitly instead of manipulating state ad hoc.
- Pin reusable module versions and constrain Terraform and providers intentionally. Commit `.terraform.lock.hcl` for root modules and review dependency upgrades separately.
- Treat state and saved plans as sensitive. A `sensitive` flag redacts display but does not remove a value from state.
- Do not install Terragrunt, linters, scanners, policy engines, or documentation generators unless the repository already uses them or the user requests them.
- Do not use `-target` as a routine workflow. Reserve it for exceptional recovery or explicitly scoped operations and explain the incomplete-plan risk.

## Safety guardrails

- Never run `terraform apply`, `terraform destroy`, state mutation commands, import, force-unlock, workspace changes, backend migration, or tests that create infrastructure without explicit user authorization.
- Never approve a plan merely because Terraform produced it successfully. Treat unexpected replacement or destruction as a blocker pending investigation.
- Never commit state, saved plan files, `.terraform/`, credentials, secret variable files, or generated private keys.
- Never place credentials in provider blocks. Prefer the provider's standard credential chain and short-lived identity.
- Never weaken IAM, encryption, network boundaries, deletion protection, logging, or lifecycle safeguards just to make a plan pass.
- Never edit `.terraform.lock.hcl` manually; update it through Terraform and review the resulting checksums and selected versions.

## Repository profile

For this repository, preserve these defaults unless the user explicitly changes them:

- Work in `infra/terraform` and preserve the spelling `shortner`, the table `url-shortner`, and the existing AWS names and domains.
- Honor `required_version = ">= 1.7.0"` and the AWS provider constraint resolved by the configuration and lock file; do not silently upgrade either.
- Keep backend, frontend, OpenAPI, and Terraform aligned when an API behavior or public endpoint changes.
- Do not add DynamoDB indexes, TTL fields, analytics counters, or schema changes unless explicitly requested.
- Run `terraform fmt` for formatting and `terraform validate` for validation when Terraform is available and initialization can be performed safely. Never run `terraform apply` without an explicit request.

## Delivery standard

Conclude with the infrastructure behavior changed, state or replacement impact, security and cost implications, files affected, verification performed, and concrete residual risk. For reviews, lead with actionable findings ordered by severity and include precise file locations.
