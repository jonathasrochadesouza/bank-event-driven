# AI Harness

Shared operating context for AI coding tools working in this repository.

This folder is intentionally tool-neutral. Claude, Codex, OpenCode, Cursor,
and other agents can read the same source of truth before making changes.

## Structure

- `context/`: Stable project facts and development commands.
- `instructions/`: Cross-tool working rules.
- `skills/`: Reserved empty folder for future skill material.
- `tools/`: Adapter notes for specific AI tools.

## Recommended Startup

Before editing code, an AI tool should read these files:

1. `ai/context/project.md`
2. `ai/instructions/repository-rules.md`
3. The adapter in `ai/tools/` that matches the running tool

The root `AGENTS.md` remains the Codex-style entry point. This `ai/` folder is
the portable harness for every other AI assistant or automation runner.
