---
name: committing-changes
description: Use when the user explicitly asks to create a local Git commit, commit staged changes, or commit directly to main in the Assinatur repository.
---

# Committing Staged Changes

Create one local commit from the exact Git index, then push it to `main`. Do not stage, unstage, amend, open a pull request, switch branches, or inspect unstaged or untracked file content.

## Required workflow

1. Confirm that the user explicitly requested a commit. This personal repository permits direct commits and pushes to `main`.
2. Prefer `@github` MCP. Do not inspect MCP configuration first. Use it only if it can inspect the local Git index, create the exact local commit, and push that commit to `main`.
3. Fall back to Git CLI if MCP is unavailable, fails, or cannot access the local index. Never substitute remote state for staged local changes.
4. Confirm the current branch is exactly `main`. Otherwise, stop and ask the user; never switch branches.
5. Analyze only the index: `git diff --cached --name-status`, `git diff --cached --stat`, `git diff --cached --check`, and staged text patches. Use staged metadata for binaries. Stop if the index is empty.
6. Read every commit subject in the repository history with `git log --all --format='%s'`. From subjects matching `^#([0-9]+)\s+-`, extract every number, compare them numerically, and use the highest number plus one. With Git CLI, obtain the maximum using `git log --all --format='%s' | sed -nE 's/^#([0-9]+)[[:space:]]+-.*/\1/p' | sort -n | tail -1`. Use `#1` only when this produces no number. Do not infer the next number from the latest commit or the first numbered subject returned by the log.
7. Select `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, `build`, or `perf`; add a scope only when clear. Write a lowercase, imperative summary without a period; keep the full subject within 72 characters.
8. Use `#<next-number> - <type>(<optional-scope>): <brief summary>`.
9. Stop if the staged patch visibly contains a credential, private key, token, secret, whitespace error, or more than one logical change.
10. Commit only the existing index to local `main`; do not amend or alter unrelated working-tree state. Verify the commit hash and subject.
11. Push the verified commit to `main`. Prefer `@github` MCP when it supports this exact local commit; otherwise run `git push origin main`.
12. If the push is rejected or unavailable, stop and report the error. Do not force-push, pull, rebase, choose another remote, or retry a different branch.

## Scope guide

| Changed area | Scope |
| --- | --- |
| `ai/` | `ai` |
| `brand-kit/` | `brand-kit` |
| `engineering/` | `engineering` |
| `mvps/` | `mvp` |
| Multiple areas | omit |

## Guardrails

- Never run `git add`, `git add -A`, `git commit -a`, `git reset`, or `git commit --amend`.
- Never read `git diff` without `--cached` to determine the message.
- Never create task `#0`; use `#1` only when the entire repository commit history has no matching numbered subject.
- Never create a remote commit as a substitute for the local staged index.
- Never force-push, push a branch other than `main`, or create a pull request.

## Delivery

After a successful push, report:

```text
Committed and pushed to main:
#<number> - <type>(<optional-scope>): <brief summary>
<commit-hash>
```

If the commit succeeds but the push fails, report the local commit hash and the push error.
