# State, Security, and Operations

Use this reference for backends, sensitive data, credentials, plans, applies, imports, state-address changes, drift, or destructive operations.

## Protect state and plans

- Use a remote backend for shared or production infrastructure, with encryption in transit and at rest, restrictive access, locking when supported, versioning, audit logging, and tested recovery.
- Bootstrap backend infrastructure separately when required. Do not create a circular dependency where a state snapshot owns the backend that stores itself.
- Never commit `terraform.tfstate*`, crash logs, saved plans, `.terraform/`, populated secret variable files, or credentials.
- Treat saved plans as secrets: they can contain full configuration and sensitive values. Store them briefly in protected CI artifacts and apply the exact reviewed plan in automation.
- Back up state before an authorized high-risk migration and confirm the backend's recovery mechanism. Never edit state JSON manually.

## Handle identity and secrets

- Use short-lived workload identity, role assumption, or the provider's standard credential chain. Do not hardcode access keys or tokens in HCL.
- Grant Terraform and CI the least privileges required for the owned composition. Separate plan/read permissions from apply permissions when practical.
- Retrieve secrets from a dedicated secret manager when a provider resource needs them. Verify whether the chosen data source or resource argument persists the value in state.
- Use `sensitive = true` for redaction. On compatible Terraform and provider versions, prefer ephemeral values and write-only arguments when the value must not be persisted.
- Avoid broad IAM wildcards, public network exposure, disabled encryption, or unrestricted security-group rules unless the requirement is explicit and justified.

## Plan before changing infrastructure

1. Run initialization without upgrading dependencies unless the upgrade is intentional.
2. Select the correct root module, backend, workspace or environment, account, region, and variable set.
3. Run a speculative plan for review. For automation, create a saved plan and apply that exact artifact after approval.
4. Inspect create, update, replace, and destroy actions; unknown values; IAM and network changes; encryption; retention; quotas; downtime; data migration; and cost.
5. Stop on unexplained drift, unexpected replacement, destruction, privilege expansion, or a target mismatch.

Never use `-auto-approve` to bypass a required review. Never use `-refresh=false` merely to hide drift. Avoid routine `-target`; it can produce an incomplete result.

## Refactor and adopt resources safely

- Preserve resource addresses during ordinary refactors.
- Use `moved` blocks to record address changes in code, including `count` or `for_each` migrations. Review a plan proving move actions rather than destroy/create.
- Use `import` blocks for repeatable, reviewable adoption when compatible with the repository's Terraform version. Add matching configuration before import and inspect the post-import plan.
- Use `removed` blocks for intentional de-management when supported and appropriate. Distinguish removing from state from destroying the remote object.
- Use CLI `terraform state` commands only for exceptional repairs with explicit authorization, a verified target, a backup, and a documented recovery path.
- Use `force-unlock` only after proving no active operation owns the lock and obtaining explicit authorization.

## Respond to drift and failure

- Investigate whether drift came from an authorized external controller, manual change, provider normalization, or stale configuration.
- Encode intended external ownership with a narrow, documented boundary; otherwise reconcile through Terraform rather than preserving accidental drift.
- After an interrupted apply, refresh understanding with state inspection and a new plan. Do not blindly rerun or manually patch cloud resources.
- Confirm backups, deletion protection, replacement order, and rollback or forward-fix strategy for stateful resources before an authorized apply.

## Source basis

This guidance extends the book's [remote-state principle](https://www.terraform-best-practices.com/key-concepts#remote-state) with HashiCorp's current documentation for [remote state](https://developer.hashicorp.com/terraform/language/state/remote), [sensitive data](https://developer.hashicorp.com/terraform/language/manage-sensitive-data), and [execution plans](https://developer.hashicorp.com/terraform/cli/commands/plan).
