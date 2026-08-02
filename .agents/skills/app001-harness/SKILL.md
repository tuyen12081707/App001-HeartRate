---
name: app001-harness
description: Use when initializing, reviewing, updating, or optimizing the App001HeartRate project harness, skill catalog, SSOT, permissions, hooks, MCP, agents, or observability setup.
---

# App001 Harness

Use this as the Codex-native equivalent of a guided project-harness plugin. It
provides tiered setup without Claude-specific commands or runtime dependencies.

## Routing

1. Read `.memory/context.md`, `.memory/decisions.md`, and
   `.agents/skills/SKILLS_INDEX.md`.
2. Read `.agents/harness/config.json` and the requested tier file:
   `essential.md`, `standard.md`, or `full.md`.
3. Run only the command that matches the user request:

| Request | Command |
|---|---|
| initialize/check baseline | `./.agents/scripts/harness-init.sh` |
| choose tier interactively | `./.agents/scripts/harness-init.sh --interactive` |
| switch tier | `./.agents/scripts/harness-init.sh --tier <essential|standard|full>` |
| audit harness | `./.agents/scripts/harness-review.sh` |
| record confirmed SSOT context/decision | `./.agents/scripts/harness-update-ssot.sh <context|decision|inbox> "..."` |
| find duplication/stale references | `./.agents/scripts/harness-optimize.sh` |

## Safety rules

- Never overwrite existing files during init; `--tier` is the explicit exception
  that updates only `config.json`.
- Never install dependencies, modify external permissions, call MCP, commit, or
  push unless the user explicitly requests that action.
- Do not put secrets, tokens, personal data, or network credentials in config or
  memory.
- Treat `AGENTS.md`, `.memory/*`, `BRAINSTORM.md`, `SKILLS_INDEX.md`, and verified
  project code as the SSOT set; report conflicts instead of silently choosing.
- When adding or changing a repo-local skill, update `SKILLS_INDEX.md` in the same
  task and validate its YAML frontmatter.

## Tier selection

- **Essential:** baseline files and safe defaults.
- **Standard:** recommended daily development setup for this KMP repository.
- **Full:** team/autonomous-work audit including hooks, permissions, MCP, agents,
  SSOT, observability, and skill quality.

Handoffs from this skill are always written in English and include changed files,
commands/results, unresolved risks, and the next step.
