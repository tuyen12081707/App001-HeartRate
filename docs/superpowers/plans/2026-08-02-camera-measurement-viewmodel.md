# Camera Measurement ViewModel Implementation Plan

> **For agentic workers:** Execute this plan inline with test-first checkpoints.

**Goal:** Move camera measurement orchestration out of `CameraMeasurementScreen` into a testable shared ViewModel.

**Architecture:** `CameraMeasurementViewModel` receives `CameraHeartRateSensor` through constructor injection, exposes immutable `CameraMeasurementUiState`, accepts start/stop intents, and emits terminal measurement side effects. The Composable obtains the ViewModel with `koinViewModel()`, renders state, sends lifecycle intents, and handles navigation side effects.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, AndroidX Lifecycle ViewModel, Kotlin Flow, Koin.

## Global Constraints

- Shared code remains in `commonMain`; platform camera implementations remain in `androidMain`/`iosMain`.
- ViewModel must extend `BaseViewModel<S, I, E>`.
- Koin remains the DI wiring mechanism; UI must not resolve `CameraHeartRateSensor` directly.
- Sensor resources must stop on Stop intent, terminal measurement, and ViewModel clearing.
- Existing camera outcome semantics (`Completed` for valid completed BPM, `Failed` for failed/error states) remain unchanged.

### Task 1: Add failing ViewModel tests

**Files:**
- Create: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementViewModelTest.kt`

- [ ] Test that a started sensor emission updates the immutable UI state.
- [ ] Test that a completed measurement emits one `Completed` side effect and stops the sensor.
- [ ] Test that stopping the ViewModel session cancels collection and calls the sensor stop operation.
- [ ] Run the focused common test and confirm it fails because the ViewModel does not exist yet.

### Task 2: Implement ViewModel and Koin registration

**Files:**
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/di/Koin.kt`

- [ ] Define `CameraMeasurementUiState`, `CameraMeasurementIntent.Start/Stop`, and `CameraMeasurementSideEffect.Completed/Failed`.
- [ ] Collect `CameraHeartRateSensor.startMeasurement()` in `viewModelScope`, map emissions to state, guard terminal side effects, and release the sensor.
- [ ] Register `factory { CameraMeasurementViewModel(get()) }` in `presentationModule`.
- [ ] Run the focused tests and confirm they pass.

### Task 3: Refactor the Composable

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementScreen.kt`

- [ ] Replace `koinInject<CameraHeartRateSensor>()` and direct Flow collection with a `koinViewModel()` parameter and `uiState` collection.
- [ ] Send Start/Stop intents from the screen lifecycle.
- [ ] Collect ViewModel side effects and invoke existing navigation callbacks exactly once per terminal outcome.
- [ ] Keep rendering and animation behavior unchanged apart from reading the ViewModel state.

### Task 4: Verify and update project map

**Files:**
- Modify: `.agents/skills/project-context/SKILL.md`

- [ ] Run `./gradlew :shared:testAndroidHostTest` and `./gradlew :shared:compileKotlinAndroid`.
- [ ] Update the presentation map with the new ViewModel signature and camera data flow.
- [ ] Report any iOS verification limitation separately.
