# App001HeartRate Skills Appendix

Use this index as the first routing step. Do not read every skill for every task.
Read the matching `SKILL.md` only after selecting the task area below.

| Skill | Use when | Read with |
|---|---|---|
| `app001-delivery-workflow` | Any implementation, fix, review, commit, or shipping task | `AGENTS.md`, then this index |
| `kmp-development` | `commonMain`, `androidMain`, `iosMain`, Compose, Koin, SQLDelight, Ktor, navigation, or tests | `project-context` only for affected code |
| `project-context` | You need existing class names, signatures, data flow, DI, or navigation facts | The specific section/file relevant to the task |
| `app001heartrate` | Feature recipes or project-specific API, UI, database, sensor, or DI constraints | `project-context` when implementation details are needed |
| `navigation` | Adding or changing routes, screens, back behavior, or bottom-bar visibility | `kmp-development` for KMP changes |
| `ui-components` | Reusing or creating shared Compose components | `kmp-development` for broader UI changes |
| `flow-to-design` | Creating/updating flow, UI spec, acceptance criteria, or design files | `project-context` for screen mapping |
| `android-adb-e2e-test` | Installing, launching, reproducing, or verifying on Android emulator/device | `kmp-development` only if code changes are involved |

## Routing rules

1. Always read `.memory/context.md` and `.memory/decisions.md` because repository
   instructions require them.
2. Always read `app001-delivery-workflow` for a change/commit/shipping task.
3. Select the smallest set of domain skills from the table; never bulk-read all
   skills “just in case”.
4. If a new skill is added under `.agents/skills/`, update this appendix in the
   same task with its trigger and read dependencies.
5. If two skills overlap, prefer the more specific skill and use the other only
   for its referenced section.
