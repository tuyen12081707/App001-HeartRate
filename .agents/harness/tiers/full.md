# Full Harness Tier

Target: about 30 minutes. Use when the repository is being prepared for a team or
for repeated autonomous work.

Complete Standard, then audit these opt-in pillars:

- [ ] **Memory:** confirm context/decision/inbox update rules and retention scope.
- [ ] **Permissions:** review shell, filesystem, network, and external write limits
      in `AGENTS.md`; default to least privilege.
- [ ] **Hooks:** inspect `.githooks`; install only with explicit user approval.
- [ ] **MCP:** inventory configured MCP/connectors; keep secrets outside the repo.
- [ ] **Agents:** review repo-local skills and any delegated-agent policy; do not
      spawn agents for sequential or trivial work.
- [ ] **SSOT:** verify `AGENTS.md`, `SKILLS_INDEX.md`, memory, `BRAINSTORM.md`, and
      `project-context` have no conflicting policy.
- [ ] **Observability:** record verification commands, failures, blockers, and
      commit hashes in handoffs; do not log secrets or personal data.
- [ ] **Skills:** validate frontmatter, trigger descriptions, references, and index
      coverage for every `SKILL.md`.

Run all four harness scripts and the verification commands relevant to the current
code change. Full tier does not imply auto-commit, auto-push, dependency install,
or network access.
