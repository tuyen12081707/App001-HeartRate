# App001HeartRate Harness

This is the Codex-native project harness inspired by ShipWithAI's guided
`shipwithai-starter` setup. It uses Markdown, JSON, shell scripts, and repo-local
skills only; there is no Claude runtime, build step, network dependency, or secret
stored in the harness.

## Tiers

- **Essential (~5 min):** repository rules, memory, skill routing, and safe defaults.
- **Standard (~15 min):** Essential plus KMP delivery workflow, SSOT routing, and
  verification matrix. This is the default tier for this project.
- **Full (~30 min):** Standard plus opt-in hooks, permissions review, MCP/agent
  inventory, observability checklist, and skill quality review.

Read the tier file before changing harness behavior:

- [essential.md](tiers/essential.md)
- [standard.md](tiers/standard.md)
- [full.md](tiers/full.md)

## Commands

Run from the repository root:

```bash
./.agents/scripts/harness-init.sh
./.agents/scripts/harness-init.sh --interactive
./.agents/scripts/harness-init.sh --tier full
./.agents/scripts/harness-review.sh
./.agents/scripts/harness-update-ssot.sh context "short confirmed context"
./.agents/scripts/harness-optimize.sh
```

Use `--interactive` in a terminal to choose a tier from a numbered menu. Use
`--tier` for CI or non-interactive scripts.

All commands are safe by default: they do not commit, push, install dependencies,
or change permissions outside the harness scripts. The explicit `--tier` option
updates only `config.json`.

## Sources of truth

The routing order is `AGENTS.md` → `.memory/*` →
`.agents/skills/SKILLS_INDEX.md` → only the relevant skill → verified project code.
When a new skill is added, update `SKILLS_INDEX.md` in the same task.
