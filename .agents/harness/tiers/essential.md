# Essential Harness Tier

Target: about 5 minutes. Use this for a new checkout or a quick harness health
check.

## Checklist

- [ ] `AGENTS.md` exists and contains repository-level instructions.
- [ ] `.memory/context.md` and `.memory/decisions.md` exist.
- [ ] `.agents/skills/SKILLS_INDEX.md` exists and routes skills conditionally.
- [ ] `project-context`, `kmp-development`, and `app001-delivery-workflow` exist.
- [ ] `config.json` has `autoCommit: false`, `autoPush: false`, and
      `overwriteExistingFiles: false`.
- [ ] Run `./.agents/scripts/harness-review.sh`.

## Safety boundary

Essential setup never writes secrets, never installs dependencies, never pushes,
and never replaces an existing file.
