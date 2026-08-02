# Standard Harness Tier

Target: about 15 minutes. This is the recommended tier for App001HeartRate.

Complete Essential, then verify:

- [ ] `app001-delivery-workflow` is the default implementation/review workflow.
- [ ] `SKILLS_INDEX.md` contains every repo-local skill and its trigger.
- [ ] `BRAINSTORM.md`, `.memory/*`, and `project-context` are treated as SSOT
      artifacts with explicit update rules.
- [ ] Verification commands cover common tests, Android compile, iOS compile when
      supported, and Android app assembly.
- [ ] `.agents/workflows/GIT_COMMIT_RULES.md` is used for commit messages.
- [ ] `.agents/workflows/AUTO_WORKFLOW.md` is used only after explicit user request.
- [ ] Run `harness-review.sh` and `harness-optimize.sh`.

## Standard operating rule

Load the appendix first and read only task-relevant skills. Keep unrelated working
tree changes out of the task and out of commits.
