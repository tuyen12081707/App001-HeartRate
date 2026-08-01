# Heart Rate MVP Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the shared presentation/navigation boundaries and deliver the approved Calm clinical heart-rate MVP flow on Android while keeping the shared iOS target compilable.

**Architecture:** Keep SQLDelight, Clean Architecture, Koin, and state-based navigation. Make SQLDelight the single local source of truth, expose typed record IDs and dashboard data through use cases, and make each MVP ViewModel render the shared `DataState<T>` contract. Replace transient navigation values in `App.kt` with typed routes without rewriting out-of-scope News/Profile/Blood Pressure implementations.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.6.11, Kotlin 2.0.0, SQLDelight 2.0.2, Koin 4.0.0, Ktor for existing News only, Kotlin Coroutines, Material 3, common/Android host tests, Android Gradle Plugin 9.0.1.

## Global Constraints

- Demo target is Android portrait on approximately 360–430 dp; the shared iOS framework must still compile.
- The core flow is `Disclaimer → Dashboard → manual heart-rate entry → Result → History/Statistics`.
- Keep four tabs: Dashboard, History, News, Profile. News/Profile retain current behavior and cannot block the local heart-rate flow.
- Core heart-rate features work offline; News network errors are isolated and retryable.
- Seed seven days of synthetic heart-rate data once in debug/demo builds only; release builds never seed.
- Reuse `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/DataState.kt`; do not create a duplicate sealed state.
- History supports confirmed deletion; record editing, camera, blood pressure, blood sugar, tablet layouts, and landscape-specific layouts are out of scope.
- Domain owns BPM validation and statistics; Composables do not hard-code business thresholds.
- Every task ends with a focused test/build command and a focused commit.

## File Map

### Data and domain

- Modify `shared/src/commonMain/sqldelight/com/tdev/heartrate/shared/data/database/HeartRateDatabase.sq` to add record lookup and app metadata queries.
- Create `shared/src/commonMain/sqldelight/com/tdev/heartrate/shared/data/database/2.sqm` for the metadata table migration.
- Modify `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/repository/HeartRateRepository.kt` and `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/data/repository/HeartRateRepositoryImpl.kt` to return inserted IDs and load one record.
- Create `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/repository/AppMetadataRepository.kt` and `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/data/repository/AppMetadataRepositoryImpl.kt` for consent/seed markers.
- Create `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/utils/Clock.kt` with `Clock.nowMillis()` and a `SystemClock` backed by existing `getCurrentTimeMillis()`.
- Modify `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/usecase/AddHeartRateRecordUseCase.kt`; create `GetHeartRateRecordUseCase.kt`, `GetDashboardDataUseCase.kt`, `AcceptDisclaimerUseCase.kt`, `GetDisclaimerStatusUseCase.kt`, and `SeedDemoHeartRateUseCase.kt` in the same `domain/usecase` directory.
- Create `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/model/DashboardData.kt` and `DashboardPoint.kt`.
- Modify `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/di/Koin.kt` and platform modules for the new dependencies.

### Presentation and navigation

- Reuse `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/DataState.kt`; migrate MVP ViewModels to it.
- Create `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppRoute.kt`, `AppNavigator.kt`, and `MainTab.kt`.
- Create `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/result/ResultViewModel.kt`.
- Modify root `shared/src/commonMain/kotlin/com/tdev/heartrate/App.kt` to use the navigator and typed routes.
- Modify the existing `dashboard`, `history`, `add`, `result`, and `disclaimer` ViewModel/Screen files under `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/`.
- Move the reusable chart implementation from `presentation/home/HeartRateChart.kt` to `presentation/components/HeartRateChart.kt`; update `HomeScreen.kt` imports so News remains buildable.
- Modify `presentation/components/CustomBottomBar.kt`, `presentation/theme/Color.kt`, `Theme.kt`, and common resource strings for the Calm clinical shell.

### Verification and documentation

- Add common tests under `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/domain/`, `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/`, and `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/data/` for domain/use cases/ViewModels.
- Add Android database integration coverage at `androidApp/src/androidTest/kotlin/com/tdev/heartrate/HeartRatePersistenceTest.kt` if the host test cannot construct an Android driver.
- Modify `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, and `gradle/libs.versions.toml` only for test dependencies required by the plan.
- Update `docs/design/flows/app-navigation.mmd` and the affected `docs/design/features/record-editor/record-editor.md` mapping after navigation changes.

---

### Task 1: Establish typed heart-rate persistence contracts

**Files:**
- Modify: `shared/src/commonMain/sqldelight/com/tdev/heartrate/shared/data/database/HeartRateDatabase.sq`
- Create: `shared/src/commonMain/sqldelight/com/tdev/heartrate/shared/data/database/2.sqm`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/repository/HeartRateRepository.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/data/repository/HeartRateRepositoryImpl.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/repository/AppMetadataRepository.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/data/repository/AppMetadataRepositoryImpl.kt`
- Test: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/data/HeartRateRepositoryContractTest.kt` and its `FakeAppMetadataRepository` fixture.

**Interfaces:**
- `HeartRateRepository.insertRecord(record: HeartRateRecord): Long`
- `HeartRateRepository.getRecordById(id: Long): HeartRateRecord?`
- `AppMetadataRepository.get(key: String): String?`
- `AppMetadataRepository.put(key: String, value: String)`

- [ ] **Step 1: Write failing contract tests** for insert ID, lookup by ID, and metadata get/put using an in-memory fake repository. Assert that the inserted ID is passed back and a missing ID returns `null`.
- [ ] **Step 2: Run the focused tests** with `./gradlew :shared:testAndroidHostTest --tests '*HeartRateRepositoryContractTest'`; confirm they fail because the new methods do not exist.
- [ ] **Step 3: Add SQLDelight queries.** Add `getRecordById`, `getMetadata`, `upsertMetadata`, and an `AppMetadataEntity` table to `HeartRateDatabase.sq`; add the same schema change to `2.sqm`.
- [ ] **Step 4: Implement the repository contracts.** Add a SQLDelight `lastInsertRowId` query, call it immediately after `insertRecord` on the same database connection, return that ID, and map the nullable lookup through `HeartRateMapper`.
- [ ] **Step 5: Implement `AppMetadataRepositoryImpl`** on `HeartRateDatabase.appMetadataQueries` and make writes idempotent by key.
- [ ] **Step 6: Run the focused tests again** and then `./gradlew :shared:compileAndroidMain`; expect PASS and regenerated database interfaces.
- [ ] **Step 7: Commit** `feat: add typed heart rate persistence contracts`.

### Task 2: Add deterministic time, dashboard aggregation, and demo seeding

**Files:**
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/utils/Clock.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/model/DashboardPoint.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/model/DashboardData.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/usecase/AddHeartRateRecordUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/usecase/GetHeartRateRecordUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/usecase/GetDashboardDataUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/usecase/SeedDemoHeartRateUseCase.kt`
- Test: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/domain/usecase/GetDashboardDataUseCaseTest.kt`
- Test: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/domain/usecase/SeedDemoHeartRateUseCaseTest.kt`
- Test: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/domain/usecase/AddHeartRateRecordUseCaseTest.kt`

**Interfaces:**
- `fun interface Clock { fun nowMillis(): Long }`
- `data class DashboardPoint(val dayStartMillis: Long, val averageBpm: Int, val recordCount: Int)`
- `data class DashboardData(val latest: HeartRateRecord?, val averageBpm: Int, val minBpm: Int, val maxBpm: Int, val totalRecords: Int, val points: List<DashboardPoint>)`
- `GetDashboardDataUseCase.invoke(): Flow<DashboardData>`
- `suspend GetHeartRateRecordUseCase.invoke(id: Long): HeartRateRecord?`
- `SeedDemoHeartRateUseCase.invoke(): Boolean` where `true` means a seed was inserted and `false` means the marker already existed.
- `suspend AddHeartRateRecordUseCase.invoke(bpm: Int, measureType: MeasureType, bodyState: BodyState, note: String?, timestamp: Long? = null): Long`

- [ ] **Step 1: Write tests** for empty dashboard data, 7-day filtering, average/min/max/count, chart points ordered oldest-to-newest, idempotent seed, and injected timestamp use.
- [ ] **Step 2: Run `./gradlew :shared:testAndroidHostTest --tests '*GetDashboardDataUseCaseTest' --tests '*SeedDemoHeartRateUseCaseTest'`; confirm the new APIs fail to compile.
- [ ] **Step 3: Implement `Clock`** with `SystemClock` calling `getCurrentTimeMillis()` and inject it as a Koin singleton. Update `AddHeartRateRecordUseCase` to accept an optional `timestamp: Long = clock.nowMillis()` so tests and seed data are deterministic.
- [ ] **Step 4: Implement `GetDashboardDataUseCase`.** Collect `HeartRateRepository.getAllRecords()`, filter records to the last seven calendar days using the injected clock, calculate aggregates, and map one average point per day.
- [ ] **Step 5: Implement `SeedDemoHeartRateUseCase`.** Check `demo_seed_v1` through `AppMetadataRepository`; if absent, insert seven manual records with fixed BPM values and timestamps relative to `clock.nowMillis()`, then write the marker in the same coordinator transaction boundary.
- [ ] **Step 6: Implement `GetHeartRateRecordUseCase`** and return the inserted ID from `AddHeartRateRecordUseCase`.
- [ ] **Step 7: Run the focused tests and `./gradlew :shared:testAndroidHostTest`; expect PASS.**
- [ ] **Step 8: Commit** `feat: add deterministic dashboard data and demo seed`.

### Task 3: Add startup consent/demo coordination and Koin wiring

**Files:**
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/model/AppConfig.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/model/StartupData.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/usecase/AcceptDisclaimerUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/domain/usecase/GetDisclaimerStatusUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/AppStartupCoordinator.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/di/Koin.kt`
- Modify: `androidApp/src/main/kotlin/com/tdev/heartrate/MainActivity.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/App.kt`

**Interfaces:**
- `data class AppConfig(val demoDataEnabled: Boolean)`
- `data class StartupData(val consentAccepted: Boolean)`
- `GetDisclaimerStatusUseCase.invoke(): Boolean`
- `AcceptDisclaimerUseCase.invoke()`
- `AppStartupCoordinator.start(): Flow<DataState<StartupData>>`

- [ ] **Step 1: Write tests** for consent default false, consent persistence after accept, demo seed called only when `AppConfig.demoDataEnabled` is true, and startup state success/error.
- [ ] **Step 2: Run `./gradlew :shared:testAndroidHostTest --tests '*Startup*'`; confirm failures.
- [ ] **Step 3: Implement consent use cases** against `AppMetadataRepository` using keys `disclaimer_accepted` and `demo_seed_v1`.
- [ ] **Step 4: Implement `AppStartupCoordinator`.** Load consent, invoke seeding only for demo config, and emit `DataState.Loading`, `Success(StartupData(consentAccepted))`, or `Error` without hiding database failures.
- [ ] **Step 5: Wire Koin** for `AppConfig`, `Clock`, metadata repository, use cases, coordinator, and new ViewModels.
- [ ] **Step 6: Pass Android debug status** from `MainActivity` using `applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0`; pass `AppConfig(demoDataEnabled = isDebuggable)`. Keep the iOS `App()` default `AppConfig(false)`.
- [ ] **Step 7: Run startup tests and `./gradlew :shared:compileKotlinIosSimulatorArm64`; expect PASS.**
- [ ] **Step 8: Commit** `feat: coordinate consent and demo startup`.

### Task 4: Refactor typed navigation and shared DataState ViewModels

**Files:**
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppRoute.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppNavigator.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/navigation/MainTab.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/result/ResultViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/dashboard/DashboardViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/history/HistoryViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/disclaimer/DisclaimerScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/App.kt`

**Interfaces:**
- `sealed interface AppRoute { data object Disclaimer; data class Main(val tab: MainTab); data object AddHeartRate; data class Result(val recordId: Long) }`
- `class AppNavigator(initialRoute: AppRoute) { val route: StateFlow<AppRoute>; fun navigate(route: AppRoute); fun back() }`
- `DashboardUiState(val data: DataState<DashboardData>)`
- `HistoryUiState(val data: DataState<List<HeartRateRecord>>)` plus `deleteState: DataState<Long>`
- `data class AddRecordUiState(val bpm: String = "", val bodyState: BodyState? = null, val note: String = "", val saveState: DataState<Long> = DataState.Idle, val fieldErrors: Map<String, String> = emptyMap())`
- `ResultUiState(val data: DataState<HeartRateRecord>)`

- [ ] **Step 1: Write ViewModel tests** for dashboard/history Flow mapping, Add Record validation and duplicate-save guard, Result loading by ID, and delete failure preservation.
- [ ] **Step 2: Run `./gradlew :shared:testAndroidHostTest --tests '*ViewModelTest'`; confirm failures.
- [ ] **Step 3: Implement `AppRoute`, `MainTab`, and `AppNavigator`** with one-way route updates and no whole-object arguments.
- [ ] **Step 4: Migrate Dashboard and History ViewModels** from ad-hoc `isLoading/isEmpty` flags to `DataState`, retaining records when a delete operation fails.
- [ ] **Step 5: Migrate Add Record ViewModel** so `saveState` is `Idle`/`Loading`/`Success(recordId)`/`Error`, while BPM/body-state/note fields remain intact after errors.
- [ ] **Step 6: Implement `ResultViewModel`** that loads `recordId` and maps missing records to a user-visible `DataState.Error`.
- [ ] **Step 7: Replace `currentScreen`, `prefilledBpm`, `lastSavedBpm`, `lastSavedBodyState`, and route lambdas in `App.kt`** with the navigator; keep News/Profile routes and existing out-of-scope screens reachable without adding them to the MVP path.
- [ ] **Step 8: Run ViewModel tests and `./gradlew :shared:compileAndroidMain`; expect PASS.**
- [ ] **Step 9: Commit** `refactor: use typed app routes and shared data state`.

### Task 5: Implement the Calm clinical UI shell and MVP screens

**Files:**
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/components/EmptyState.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/components/FeatureErrorState.kt`
- Move/modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/home/HeartRateChart.kt` → `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/components/HeartRateChart.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/dashboard/DashboardScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/result/ResultScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/history/HistoryScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/disclaimer/DisclaimerScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/components/CustomBottomBar.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/theme/Color.kt`, `Theme.kt`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/home/HomeScreen.kt`

**Interfaces:**
- `DashboardScreen(viewModel: DashboardViewModel, onAdd: () -> Unit, onTabSelected: (MainTab) -> Unit)`
- `AddRecordScreen(viewModel: AddRecordViewModel, onBack: () -> Unit, onSaved: (Long) -> Unit)`
- `ResultScreen(viewModel: ResultViewModel, onDashboard: () -> Unit, onAddAnother: () -> Unit)`
- `HistoryScreen(viewModel: HistoryViewModel, onTabSelected: (MainTab) -> Unit)`

- [ ] **Step 1: Add/adjust resource strings** for Dashboard statistics, validation, empty/error/retry, delete confirmation, and the Calm clinical tab labels; remove hard-coded MVP copy from the changed screens.
- [ ] **Step 2: Implement reusable Empty/Error components** that accept `message: String`, `actionLabel: String`, and `onAction: () -> Unit`.
- [ ] **Step 3: Move the chart** to `presentation/components/HeartRateChart.kt` with `HeartRateChart(points: List<DashboardPoint>, modifier: Modifier = Modifier)`. In `HomeScreen`, map each raw history BPM to `DashboardPoint(dayStartMillis = index.toLong(), averageBpm = bpm, recordCount = 1)` before calling the shared component.
- [ ] **Step 4: Redesign Dashboard** with latest BPM, average/min/max/count cards, seven-day chart, empty state, loading state, error Retry, and one Add reading CTA. Do not render blood pressure/blood sugar cards in the MVP dashboard.
- [ ] **Step 5: Redesign Add Record** around the approved focused form, keeping wheel BPM input, body-state chips, note, inline 30–250 validation, disabled Save while `DataState.Loading`, and no camera CTA.
- [ ] **Step 6: Redesign Result** to render the saved record from `ResultViewModel`; provide Back to dashboard and Add another reading without reusing transient BPM state.
- [ ] **Step 7: Redesign History** with day grouping, row details, Delete confirmation, empty/loading/error states, and delete Retry. Use `LazyColumn` keys from `record.id`.
- [ ] **Step 8: Align Disclaimer, bottom bar, colors, typography, and News/Profile shell** to the Calm clinical theme without changing their network/business behavior.
- [ ] **Step 9: Run `./gradlew :shared:compileAndroidMain :androidApp:assembleDebug`; expect PASS and inspect the generated APK on a 360–430 dp portrait emulator.**
- [ ] **Step 10: Commit** `feat: redesign heart rate MVP screens`.

### Task 6: Update navigation/design documentation and add integration verification

**Files:**
- Modify: `docs/design/flows/app-navigation.mmd`
- Modify: `docs/design/features/record-editor/record-editor.md`
- Create: `androidApp/src/androidTest/kotlin/com/tdev/heartrate/HeartRatePersistenceTest.kt` only if host tests cannot create an Android SQLDelight driver.
- Modify: `androidApp/build.gradle.kts` and `gradle/libs.versions.toml` only if Android test dependencies are required.

**Interfaces:**
- Documentation must map `AppRoute`/`MainTab` to actual Composables and ViewModels.
- Integration test must verify `insert → query → delete` with a test database name and no production database mutation.

- [ ] **Step 1: Update the Mermaid flow** to show typed `MainTab`, `AddHeartRate → Result(recordId)`, confirmed delete, and the fact that News/Profile remain separate tabs.
- [ ] **Step 2: Update the record-editor spec** to mark editing as out of MVP scope and map the create-only route to the new navigator.
- [ ] **Step 3: Run `./gradlew :shared:testAndroidHostTest`; if an Android driver is unavailable in host tests, add the targeted Android test and the existing AndroidX test dependencies, then run `./gradlew :androidApp:testDebugUnitTest`.
- [ ] **Step 4: Commit** `docs: align MVP navigation specs and persistence verification`.

### Task 7: Full verification and release-demo checklist

**Files:**
- Modify: none unless verification finds a concrete defect.
- Test: existing common/domain/ViewModel tests and the Android integration test from Task 6.

- [ ] **Step 1: Run Android tests:** `./gradlew :shared:testAndroidHostTest :androidApp:testDebugUnitTest`.
- [ ] **Step 2: Run Android build:** `./gradlew :androidApp:assembleDebug`.
- [ ] **Step 3: Run iOS shared compilation:** `./gradlew :shared:compileKotlinIosSimulatorArm64`.
- [ ] **Step 4: Execute the manual checklist** on a portrait 360–430 dp emulator: first launch seed/consent, valid and invalid manual entry, Result, Dashboard update, History delete, restart persistence, and News Retry with network disabled.
- [ ] **Step 5: Run `git diff --check` and `git status --short`; verify `.superpowers/` is ignored and no generated build output is staged.
- [ ] **Step 6: Commit only a concrete verification fix** if one is required; otherwise record the verification output in the handoff without creating an empty commit.

## Plan Self-Review

- Every spec requirement maps to Tasks 1–7: persistence/ID (1), dashboard/seed/time (2–3), typed navigation/DataState (4), Calm clinical screens (5), offline/error behavior (3–5), documentation (6), and Android/iOS verification (7).
- No task contains a placeholder or an undefined deferred action.
- Types are consistent: Task 1 introduces repository IDs and metadata; Task 2 consumes them and defines `DashboardData`; Task 3 wires startup; Task 4 consumes those use cases in ViewModels; Task 5 consumes the ViewModel state; Tasks 6–7 verify the resulting routes and persistence.
- Out-of-scope features remain buildable but are not made prerequisites for the heart-rate MVP.
