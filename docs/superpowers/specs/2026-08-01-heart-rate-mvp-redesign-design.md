# Heart Rate MVP Redesign and Presentation Refactor

**Status:** Ready for user review

**Date:** 2026-08-01

## Goal

Deliver a stable Android-first MVP while keeping the shared KMP code compilable for iOS. The complete demo flow is:

```text
Disclaimer → Dashboard → manual heart-rate entry → Result → History/Statistics
```

The visual direction is **Calm clinical**: light surfaces, calm blue-violet accents, generous spacing, and clear numeric hierarchy.

## Scope

### In scope

- Refactor presentation/navigation boundaries while preserving the working SQLDelight, repository, use-case, and Koin foundation.
- Redesign Disclaimer, Dashboard, Add Record, Result, and History around the approved Calm clinical layout.
- Keep the four-tab shell: Dashboard, History, News, Profile.
- Keep current News and Profile functionality available, but do not make them dependencies of the heart-rate demo flow.
- Make the core flow offline-first.
- Seed seven days of sample heart-rate records only in debug/demo builds, exactly once per database.
- Use the shared `DataState` contract for feature loading, success, error, and idle states.
- Support portrait Android phones around 360–430 dp and keep iOS shared compilation green.
- Allow History deletion with confirmation; editing is out of MVP scope.

### Out of scope

- Camera measurement as a required demo path.
- Blood pressure, blood sugar, or camera feature expansion.
- New News/Profile business features.
- Tablet/landscape-specific layouts.
- Full navigation rewrite for every existing feature.

## Architecture

The project remains Clean Architecture and state-based navigation:

- **Domain:** Existing heart-rate models, repository interfaces, and use cases remain platform-independent. Add only the use cases needed by the MVP (for example, observe dashboard data, load a record by ID, and seed demo data).
- **Data:** SQLDelight remains the local source of truth. Repository implementations expose `Flow` for records and return the inserted record ID. A metadata marker prevents repeated demo seeding.
- **Presentation:** Each MVP feature has a focused `Route`/screen entry point, `UiState`, `Intent`, and `ViewModel`. Composables render state and emit intents; they do not call SQLDelight or encode business thresholds.
- **Navigation:** Replace the growing collection of temporary values in `App.kt` with a typed navigator and routes. Pass IDs or small values, not whole domain objects. The main shell owns the four tabs; modal/detail routes hide the bottom bar.

The existing `DataState.kt` is the shared state contract and must be reused rather than duplicated:

```kotlin
sealed interface DataState<out T> {
    data object Idle : DataState<Nothing>
    data object Loading : DataState<Nothing>
    data class Success<T>(val data: T) : DataState<T>
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : DataState<Nothing>
}
```

## Navigation and screen responsibilities

```text
Disclaimer
  └─ accept → Main(Dashboard)

Main(Dashboard) ── add → AddHeartRate ── saved(recordId) → Result(recordId)
Main(History) ── delete(recordId) → History
Result ── back → Main(Dashboard)
Result ── add another → AddHeartRate
Main(News) and Main(Profile) retain their existing behavior inside the new shell.
```

- **Disclaimer:** one-time consent entry; after acceptance, persist the consent marker so normal launches open the main shell.
- **Dashboard:** latest reading, seven-day average/range, record count, line chart, and one primary Add reading CTA. Empty state points to Add Record.
- **Add Record:** BPM, date/time, body-state chips, optional note, inline validation, and a fixed Save CTA. Camera is not part of the MVP path.
- **Result:** confirmation of persistence, BPM/body state, Back to dashboard, and Add another reading.
- **History:** records grouped by day; each row shows BPM, timestamp, body state, and measure type. The overflow action is Delete with confirmation.
- **News/Profile:** existing feature behavior remains; they share the new theme and shell but do not block the core flow.

## Data flow and demo seed

On startup, an `AppStartupCoordinator` initializes the database and invokes `DemoDataSeeder` only when the injected build environment enables demo data. The seeder inserts fixed, clearly synthetic heart-rate records spread over the previous seven days and writes a `demo_seed_v1` marker in metadata. A release build never enables this policy.

Dashboard data flows from SQLDelight `Flow` through `HeartRateRepository` and a single dashboard use case that maps records to latest value, seven-day aggregates, and chart points. History observes the same source. Adding a record returns its generated ID; Result loads by ID, so `App.kt` does not own transient copies of saved records. Deleting a record updates both screens through the shared Flow.

## UI state and error behavior

All feature ViewModels use `DataState` with the transition `Idle → Loading → Success` or `Loading → Error`.

- Loading uses a skeleton/progress state instead of a blank screen.
- Empty states include a clear next action.
- Save validation is immediate; the Save action is disabled while invalid or in flight.
- Save failures keep entered values and expose Retry without duplicate inserts.
- Delete failures keep the row and expose Retry.
- Database initialization failure shows a blocking retry state; seed failure falls back to an empty state without inventing replacement data.
- News network failures are isolated to News and expose Retry; they cannot break local heart-rate features.

## Verification plan

- **Domain tests:** BPM constraints, statistics, seven-day filtering, idempotent seeding, and delete behavior.
- **ViewModel tests:** Idle/Loading/Success/Error transitions, preserved form values after failure, and duplicate-save protection.
- **Repository/database tests:** insert → observe → delete and persistence after restart.
- **Build checks:** `./gradlew :shared:testAndroidHostTest`, `./gradlew :androidApp:assembleDebug`, and iOS shared-framework compilation.
- **Manual Android checklist:** portrait 360–430 dp; accept disclaimer; add valid and invalid readings; confirm Result; confirm Dashboard/statistics update; delete from History; restart and verify persistence; disable network and confirm News Retry.

## Acceptance criteria

- The manual heart-rate flow completes without a crash on Android.
- Dashboard statistics and chart update from real database data after save/delete.
- Demo data appears once in debug/demo builds and never in release builds.
- History groups records by day and supports confirmed deletion.
- Every MVP feature uses the shared `DataState` contract.
- The core flow does not require network access.
- Android tests/build pass and the shared iOS target still compiles.
- No required scope item depends on camera, blood pressure, blood sugar, or new News/Profile behavior.
