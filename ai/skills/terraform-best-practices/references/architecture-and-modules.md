# Architecture and Modules

Use this reference when choosing code structure, state boundaries, modules, providers, or dependency versions.

## Model the layers

- Treat a resource as one provider-managed object.
- Treat a resource module as a small, versionable collection of related resources that performs one cohesive capability.
- Treat an infrastructure module as a project-specific assembly of resources or resource modules for one logical area such as an account and region.
- Treat a composition or root module as the deployment boundary that configures providers and a backend, supplies environment values, and owns one state snapshot.
- Exchange data inside a composition through direct references and module outputs. Across compositions, prefer a purpose-built configuration store or provider data source when it can expose only the required data. Use `terraform_remote_state` only after considering that readers can access the entire state snapshot.

## Choose boundaries deliberately

Keep one root module while resources share ownership, lifecycle, access controls, deployment cadence, and failure domain. Split state when one or more of these differ materially:

- AWS account, region, environment, or security boundary
- Team ownership or independent release cadence
- Privilege level or state-reader population
- High blast radius, slow plans, or unrelated failure recovery
- Long-lived shared foundations versus frequently deployed application infrastructure

Do not use workspaces as an automatic substitute for clear directories, accounts, or state isolation. Evaluate whether environments genuinely share the same configuration and access model.

## Structure files for discovery

For a small root module, prefer:

- `terraform.tf` or `versions.tf` for `required_version` and `required_providers`
- `providers.tf` for provider configuration
- `backend.tf` for backend configuration when present
- `main.tf` for a small cohesive graph
- `variables.tf`, `locals.tf`, and `outputs.tf` for their respective blocks

As the graph grows, group resources by coherent domain such as `network.tf`, `compute.tf`, or `storage.tf`. Terraform loads all `.tf` files in a directory together, so file splits improve navigation rather than evaluation order.

## Design modules

- Create a module only for a cohesive abstraction with a stable interface or genuine reuse. Avoid thin wrappers that merely rename every provider argument.
- Keep child modules provider-agnostic in configuration: declare provider requirements, but configure provider credentials, regions, and aliases in the root.
- Pass provider aliases explicitly through the module `providers` map.
- Prefer typed, documented inputs with validation for real domain constraints. Add reasonable defaults only when the default is safe across callers.
- Expose the smallest useful set of outputs. Do not expose entire resources or secret-bearing structures for convenience.
- Avoid hidden dependencies. Pass resource identifiers or objects into modules instead of rediscovering caller-owned resources by ambiguous tags or names.
- Use module-level `depends_on` only when a dependency cannot be represented through input expressions.

## Control dependencies

- Declare a Terraform version compatible with every language feature used.
- Declare provider source addresses and intentional version constraints in every module.
- Commit `.terraform.lock.hcl` for root configurations. Generate platform checksums with `terraform providers lock` when CI and developer platforms differ.
- Pin registry module versions. Pin Git module sources to immutable release tags or commit SHAs; do not use a moving branch for production.
- Change dependency constraints and the lock file intentionally, review provider release notes, then inspect the new plan for schema or behavior changes.

## Source basis

This model adapts the resource module → infrastructure module → composition hierarchy and small-state guidance from [Terraform Best Practices](https://www.terraform-best-practices.com/key-concepts), reconciled with HashiCorp's current [module](https://developer.hashicorp.com/terraform/language/modules/develop) and [style](https://developer.hashicorp.com/terraform/language/style) guidance.
