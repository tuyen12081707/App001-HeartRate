# Task 3 report — Startup consent/demo coordination

## Changes

- Added `AppConfig`, `StartupData`, disclaimer consent use cases, and
  `AppStartupCoordinator` with `Loading → Success/Error` Flow states.
- Wired new domain/presentation types plus configurable `AppConfig` into Koin.
- Android now derives demo mode from `ApplicationInfo.FLAG_DEBUGGABLE`; shared and
  iOS `App()` defaults keep demo data disabled.
- `App()` exposes startup state through an optional callback for the navigation work
  in Task 4.
- Updated the living project context and brainstorm status.

## Validation

- `./gradlew :shared:testAndroidHostTest --tests '*Startup*'` — PASS
- `./gradlew :shared:compileAndroidMain` — PASS
- `./gradlew :shared:compileKotlinIosSimulatorArm64` — PASS
- `./gradlew :androidApp:compileDebugKotlin` — PASS
- `git diff --check` — PASS

## Concerns

- Task 4 still owns replacing the current screen state/navigation and consuming the
  startup callback; this task intentionally leaves that refactor out.
