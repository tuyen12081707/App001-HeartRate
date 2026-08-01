# Android camera measurement hardening

## Status

Approved design. Scope is limited to the Android camera-measurement flow.

## Goal

Make camera measurement dependable for an Android demo while preserving manual entry as the fallback. A camera value must be confirmed in the existing manual-record form before it is saved.

## Scope

- Request/check Android camera permission before measurement starts.
- Start the existing `CameraHeartRateSensor` only after permission is granted.
- Show an actionable, localized state when permission is denied, unavailable, or the sensor reports an error.
- Preserve retry and back actions for failed measurements.
- On a valid completed BPM, navigate to the existing record editor with the BPM prefilled. The editor remains the single save point.
- Keep manual BPM entry available at all times.

## Out of scope

- Changing the BPM signal-processing algorithm or claiming medical accuracy.
- iOS camera changes.
- Dashboard, History, Profile, News, or visual redesign work.
- Automatically persisting a camera result.

## Flow

```text
Add Record → Open Camera
    → permission granted → Camera Measurement
        → completed(valid BPM) → Add Record (prefilled BPM) → user saves
        → no finger / failed / sensor error → actionable retry or back
    → permission denied or unavailable → actionable retry or back
```

## Architecture

- Android permission ownership stays at the Android app boundary, not in `commonMain`.
- Shared `CameraMeasurementScreen` receives only the permission-ready measurement state and emits navigation callbacks.
- The existing state-based navigation in `App.kt` carries the completed BPM to `AddRecordViewModel` as it does today.
- `CameraHeartRateSensor` remains the platform implementation that owns CameraX resources and releases them on disposal.

## Error handling

| Condition | User-visible result | Available action |
| --- | --- | --- |
| Permission not granted | Explain that camera access is required for this method | Grant/Retry or Back to manual entry |
| Camera unavailable or sensor error | Clear non-diagnostic error state | Retry or Back |
| Finger lost during measurement | Existing failed-measurement screen | Retry or Back |
| Completed BPM | Prefilled manual form, not auto-saved | Review and Save, or change manually |

## Verification

- Android unit/host tests cover permission-state to navigation decisions where platform-independent.
- Android build succeeds.
- Manual device check verifies: first permission request, denied flow, retry after permission grant, camera completion, prefilled BPM, and back-to-manual fallback.
- iOS shared code continues to compile; no iOS behavior is changed.

## Acceptance criteria

- [ ] Opening camera never starts CameraX without permission.
- [ ] Denying permission never crashes or traps the user.
- [ ] A successful camera BPM reaches Add Record as a prefilled value and is not saved until the user confirms.
- [ ] Failure, retry, and back flows work without leaking the camera session.
- [ ] Manual entry still works with no camera permission.
