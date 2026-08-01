# Android Camera Measurement Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Android camera measurement permission-safe, recoverable, and able to prefill a user-confirmed heart-rate record.

**Architecture:** `MainActivity` owns Android runtime permission and passes a continuation callback to shared `App`. Shared navigation handles camera entry, denial, retry, and manual fallback. The existing record editor remains the only save point and stores the measurement source.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, AndroidX Activity Result API, CameraX, Koin, Kotlin coroutines/Flow, kotlin.test.

## Global Constraints

- Android-only permission behavior; do not change iOS camera code or expose a camera action on iOS.
- Do not change BPM signal processing or make medical-accuracy claims.
- A camera completion prefills Add Record as `MeasureType.CAMERA_SENSOR`; it is saved only after user confirms.
- Manual entry works without camera permission.
- Keep state-based `AppNavigator`; do not add Jetpack Navigation Compose.
- Add all new user-facing copy to Compose resources.

## File structure

| File | Responsibility |
| --- | --- |
| `androidApp/src/main/kotlin/com/tdev/heartrate/MainActivity.kt` | Check/request `CAMERA` and resume granted/denied continuation. |
| `shared/src/commonMain/kotlin/com/tdev/heartrate/App.kt` | Guard camera entry/retry; connect routes and screen callbacks. |
| `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppRoute.kt` | Carry BPM/source and represent permission denial. |
| `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordViewModel.kt` | Initialize form with optional BPM/source and save preserved source. |
| `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordScreen.kt` | Render camera CTA only when Android supplies callback. |
| `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementOutcome.kt` | Map terminal sensor states to testable outcomes. |
| `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraPermissionDeniedScreen.kt` | Explain denial and offer retry/manual fallback. |
| `shared/src/commonMain/composeResources/values/strings.xml` | Permission and manual-fallback copy. |
| `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/ViewModelTest.kt` | Prefill/source persistence test. |
| `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementOutcomeTest.kt` | Terminal-state mapping test. |
| `docs/design/flows/app-navigation.mmd` | Permission and fallback navigation edges. |

---

### Task 1: Carry a completed camera result into Add Record

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppRoute.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordViewModel.kt`
- Modify: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppNavigatorTest.kt`
- Modify: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/ViewModelTest.kt`

**Interfaces:** Produces `AppRoute.AddHeartRate(prefilledBpm: Int? = null, measureType: MeasureType = MeasureType.MANUAL)`, `AppRoute.CameraPermissionDenied`, and `AddRecordIntent.ResetForNewEntry(prefilledBpm: Int? = null, measureType: MeasureType = MeasureType.MANUAL)`.

- [ ] **Step 1: Write failing tests**

    @Test
    fun cameraAddRoutePreservesPrefilledBpmAndSource() {
        val route = AppRoute.AddHeartRate(82, MeasureType.CAMERA_SENSOR)
        assertEquals(82, route.prefilledBpm)
        assertEquals(MeasureType.CAMERA_SENSOR, route.measureType)
    }

    @Test
    fun cameraEntryPrefillsBpmAndSavesCameraSource() = runTest {
        val repository = FakeHeartRateRepository()
        val viewModel = AddRecordViewModel(AddHeartRateRecordUseCase(repository, Clock { 1L }))
        viewModel.onIntent(AddRecordIntent.ResetForNewEntry(82, MeasureType.CAMERA_SENSOR))
        viewModel.onIntent(AddRecordIntent.UpdateBodyState(BodyState.RESTING))
        viewModel.onIntent(AddRecordIntent.SaveRecord)
        advanceUntilIdle()
        assertEquals(MeasureType.CAMERA_SENSOR, repository.lastInsertedRecord!!.measureType)
    }

- [ ] **Step 2: Run test to verify failure**

    Run: `./gradlew :shared:testAndroidHostTest`
    Expected: compile failure because route/reset payloads do not exist.

- [ ] **Step 3: Implement route and editor payload**

    data class AddHeartRate(
        val prefilledBpm: Int? = null,
        val measureType: MeasureType = MeasureType.MANUAL
    ) : AppRoute
    data object CameraPermissionDenied : AppRoute

    data class AddRecordUiState(
        val bpm: String = "",
        val measureType: MeasureType = MeasureType.MANUAL,
        val bodyState: BodyState? = null,
        val note: String = "",
        val saveState: DataState<Long> = DataState.Idle,
        val fieldErrors: Map<String, String> = emptyMap()
    )

    data class ResetForNewEntry(
        val prefilledBpm: Int? = null,
        val measureType: MeasureType = MeasureType.MANUAL
    ) : AddRecordIntent

    is AddRecordIntent.ResetForNewEntry -> _uiState.value = AddRecordUiState(
        bpm = intent.prefilledBpm?.toString().orEmpty(),
        measureType = intent.measureType
    )

Pass `currentState.measureType` to `AddHeartRateRecordUseCase`. Add `lastInsertedRecord: HeartRateRecord?` to `FakeHeartRateRepository`, assign it in `insertRecord`, and update the existing reset test to call `ResetForNewEntry()`.

- [ ] **Step 4: Run test to verify pass**

    Run: `./gradlew :shared:testAndroidHostTest`
    Expected: PASS.

- [ ] **Step 5: Commit**

    git add shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppRoute.kt shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordViewModel.kt shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/navigation/AppNavigatorTest.kt shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/ViewModelTest.kt
    git commit -m "feat: carry camera readings into record editor"

### Task 2: Map camera terminal states to recovery actions

**Files:**
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementOutcome.kt`
- Create: `shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementOutcomeTest.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementScreen.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/FailedScanScreen.kt`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`

**Interfaces:** Produces `CameraMeasurementOutcome.None`, `Completed(bpm: Int)`, or `Failed`; changes failed scan to `FailedScanScreen(onTryAgain, onEnterManually)`.

- [ ] **Step 1: Write failing tests**

    @Test
    fun completedStateWithBpmProducesCompletedOutcome() {
        assertEquals(
            CameraMeasurementOutcome.Completed(76),
            CameraMeasurementState(bpm = 76, state = SensorState.COMPLETED).toOutcome()
        )
    }

    @Test
    fun errorAndFailedStatesProduceFailedOutcome() {
        assertEquals(CameraMeasurementOutcome.Failed, CameraMeasurementState(state = SensorState.ERROR).toOutcome())
        assertEquals(CameraMeasurementOutcome.Failed, CameraMeasurementState(state = SensorState.FAILED).toOutcome())
    }

- [ ] **Step 2: Run focused test to verify failure**

    Run: `./gradlew :shared:testAndroidHostTest --tests '*CameraMeasurementOutcomeTest'`
    Expected: compile failure because the mapper does not exist.

- [ ] **Step 3: Implement mapper and screen actions**

    sealed interface CameraMeasurementOutcome {
        data object None : CameraMeasurementOutcome
        data class Completed(val bpm: Int) : CameraMeasurementOutcome
        data object Failed : CameraMeasurementOutcome
    }

    fun CameraMeasurementState.toOutcome(): CameraMeasurementOutcome = when {
        state == SensorState.COMPLETED && bpm > 0 -> CameraMeasurementOutcome.Completed(bpm)
        state == SensorState.FAILED || state == SensorState.ERROR -> CameraMeasurementOutcome.Failed
        else -> CameraMeasurementOutcome.None
    }

Replace `LaunchedEffect(state.state)` with `LaunchedEffect(state)` in `CameraMeasurementScreen`; dispatch `Completed` to `onMeasurementCompleted`, `Failed` to `onMeasurementFailed`, and `None` to no action. Keep `DisposableEffect` unchanged. Rename the second `FailedScanScreen` callback to `onEnterManually`, change its home CTA to manual entry, and add `failed_scan_enter_manually`.

- [ ] **Step 4: Run tests and shared compile**

    Run: `./gradlew :shared:testAndroidHostTest :shared:compileKotlinIosSimulatorArm64`
    Expected: PASS.

- [ ] **Step 5: Commit**

    git add shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera shared/src/commonMain/composeResources/values/strings.xml shared/src/commonTest/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraMeasurementOutcomeTest.kt
    git commit -m "feat: add recoverable camera measurement outcomes"

### Task 3: Request Android permission before camera entry or retry

**Files:**
- Modify: `androidApp/src/main/kotlin/com/tdev/heartrate/MainActivity.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/App.kt`
- Modify: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordScreen.kt`
- Create: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraPermissionDeniedScreen.kt`
- Modify: `shared/src/commonMain/composeResources/values/strings.xml`
- Modify: `docs/design/flows/app-navigation.mmd`

**Interfaces:** `App` receives nullable `cameraPermissionRequester: ((() -> Unit), (() -> Unit)) -> Unit`; null hides camera entry and preserves iOS behavior.

- [ ] **Step 1: Add failing App callback use in Android entrypoint**

Pass named `cameraPermissionRequester` to `App` in `MainActivity` before adding the parameter to `App`.

- [ ] **Step 2: Run build to verify failure**

    Run: `./gradlew :androidApp:compileDebugKotlin`
    Expected: compile failure: no parameter named `cameraPermissionRequester`.

- [ ] **Step 3: Implement Android requester and guarded routing**

    private data class CameraPermissionCallbacks(val onGranted: () -> Unit, val onDenied: () -> Unit)

    var pendingCallbacks by remember { mutableStateOf<CameraPermissionCallbacks?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val callbacks = pendingCallbacks
        pendingCallbacks = null
        if (granted) callbacks?.onGranted?.invoke() else callbacks?.onDenied?.invoke()
    }
    val requestCameraPermission: ((() -> Unit), (() -> Unit)) -> Unit = { onGranted, onDenied ->
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) onGranted()
        else {
            pendingCallbacks = CameraPermissionCallbacks(onGranted, onDenied)
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

Add nullable `cameraPermissionRequester` to `App`. In Add route, reset with route data, derive camera CTA only if requester is non-null, and pass it to `AddRecordScreen`.

    val openCamera = cameraPermissionRequester?.let { requester ->
        { requester(
            { navigator.navigate(AppRoute.CameraMeasurement) },
            { navigator.navigate(AppRoute.CameraPermissionDenied) }
        ) }
    }

`AddRecordScreen` accepts nullable `onOpenCamera` and renders its existing `add_record_camera_action` only when non-null. Camera completion navigates to `AppRoute.AddHeartRate(bpm, MeasureType.CAMERA_SENSOR)`. Retry in Failed Scan and Permission Denied repeats the requester; both manual buttons navigate to `AppRoute.AddHeartRate()`.

- [ ] **Step 4: Create denial UI and update flow doc**

Create `CameraPermissionDeniedScreen` with title, explanation, `Allow camera`, and `Enter manually` actions. Add these strings:

    <string name="camera_permission_denied_title">Camera permission needed</string>
    <string name="camera_permission_denied_description">Allow camera access to measure with your fingertip, or enter a reading manually.</string>
    <string name="camera_permission_retry">Allow camera</string>
    <string name="camera_enter_manually">Enter manually</string>
    <string name="failed_scan_enter_manually">Enter manually</string>

Add edges: `ADD_RECORD → permission check → CAMERA`; denied goes to permission screen; both permission-denied and failed-scan screens offer manual entry back to `ADD_RECORD`.

- [ ] **Step 5: Run builds and automated tests**

    Run: `./gradlew :shared:testAndroidHostTest :shared:compileKotlinIosSimulatorArm64 :androidApp:assembleDebug`
    Expected: BUILD SUCCESSFUL; iOS has no callback and therefore no new camera CTA.

- [ ] **Step 6: Run physical Android acceptance checks**

1. Fresh debug install → Add Record → Measure with camera: Android permission dialog appears.
2. Deny: explanation screen appears, no crash.
3. Enter manually: empty manual form works with no permission.
4. Allow: CameraX starts only after grant.
5. Complete: form receives BPM; body state is still required; record is absent until Save.
6. Fail scan or revoke permission: retry is guarded and manual fallback works.

- [ ] **Step 7: Commit**

    git add androidApp/src/main/kotlin/com/tdev/heartrate/MainActivity.kt shared/src/commonMain/kotlin/com/tdev/heartrate/App.kt shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/add/AddRecordScreen.kt shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/camera/CameraPermissionDeniedScreen.kt shared/src/commonMain/composeResources/values/strings.xml docs/design/flows/app-navigation.mmd
    git commit -m "feat: guard Android camera measurement with permission"

### Task 4: Final verification and record

**Files:**
- Modify: `docs/superpowers/specs/2026-08-02-camera-android-hardening-design.md`

**Interfaces:** Consumes Tasks 1–3 and records verified acceptance criteria.

- [ ] **Step 1: Run final verification**

    Run: `./gradlew :shared:testAndroidHostTest :shared:compileKotlinIosSimulatorArm64 :androidApp:assembleDebug`
    Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Update spec evidence**

Mark automatically verified spec criteria `[x]`. If no physical device is connected, leave only device-only checks `[ ]` and append: `Manual device verification pending: the permission dialog and CameraX sensor require physical hardware.`

- [ ] **Step 3: Inspect final diff**

    Run: `git diff --check` and `git status --short`
    Expected: no whitespace errors and only the spec verification update remains after task commits.

- [ ] **Step 4: Commit verification record**

    git add docs/superpowers/specs/2026-08-02-camera-android-hardening-design.md
    git commit -m "docs: record camera hardening verification"
