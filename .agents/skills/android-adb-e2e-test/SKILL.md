---
name: android-adb-e2e-test
description: Run and verify Android features end-to-end on an emulator or USB device with adb. Use when asked to install, launch, test, reproduce, or confirm an Android app/feature on a running device, including UI navigation, runtime permissions, notifications, services, lifecycle/background behavior, swipe-kill, or a device-level fix verification.
---

# Android E2E testing via adb

Drive the app and collect evidence that the expected state change occurred. A dispatched tap, a successful command, or a screenshot alone is not proof.

## 1. Establish the target and safety boundary

Locate adb, enumerate devices, and select exactly one serial before doing anything else.

```bash
adb devices -l
adb -s <serial> shell getprop ro.product.model
adb -s <serial> shell getprop ro.build.version.release
```

Classify the target from `adb devices -l`:

- `emulator-*`: disposable emulator; destructive reset actions are allowed only when useful to the test.
- Any other serial: physical device. Do not run `uninstall`, `pm clear`, `emu kill`, or change system settings without explicitly telling the user first.

Pass `-s <serial>` to every adb command once a target has been selected. If device state is `unauthorized` or `offline`, stop and resolve that condition instead of guessing.

Wake and unlock before taking screenshots or testing UI:

```bash
adb -s <serial> shell input keyevent KEYCODE_WAKEUP
adb -s <serial> shell dumpsys power | rg 'mWakefulness|Wakefulness'
```

## 2. Build, install, and launch

Inspect the project's Gradle modules and manifest first; do not assume the module, package, APK name, or launcher activity.

```bash
rg --files -g 'build.gradle.kts' -g 'build.gradle' -g 'AndroidManifest.xml'
./gradlew <assemble-task>
find <module>/build/outputs/apk -name '*debug*.apk' -type f
adb -s <serial> install -r <apk-path>
adb -s <serial> shell monkey -p <package-name> -c android.intent.category.LAUNCHER 1
```

Use `install -r` by default to preserve local state. On an emulator, use uninstall or `pm clear` only when the test explicitly requires a clean install/state; state the reset in the final report.

Confirm the foreground activity after launch:

```bash
adb -s <serial> shell dumpsys window | rg 'mCurrentFocus|mFocusedApp'
```

## 3. Inspect and operate the UI

Dump the accessibility tree before each action and again after every transition. Do not reuse coordinates after the UI changes.

```bash
adb -s <serial> shell uiautomator dump /sdcard/window.xml
adb -s <serial> pull /sdcard/window.xml /tmp/android-window.xml
rg 'text="[^"]*"|content-desc="[^"]*"' /tmp/android-window.xml
```

Use the node's `bounds="[x1,y1][x2,y2]"` to tap its centre:

```bash
adb -s <serial> shell input tap <center-x> <center-y>
```

Use `input text`, `keyevent`, `swipe`, or `am start` only when they match the user action being tested. Check `enabled`, `clickable`, and the parent node first; do not endlessly retry a disabled control.

When tree data is incomplete (icon-only controls, OEM UI, canvas rendering), capture and inspect a screenshot:

```bash
adb -s <serial> exec-out screencap -p > /tmp/android-screen.png
```

If the screenshot is black or empty, inspect screen wakefulness and consider `FLAG_SECURE`; do not treat screenshot blocking as an app failure without further evidence.

## 4. Verify the expected behavior

Choose evidence that directly proves the requirement. Capture a baseline before the action when that distinguishes old from new state.

| Requirement | Preferred evidence |
|---|---|
| UI/navigation | fresh UI dump plus foreground activity or visible state change |
| Database/data update | app UI after reload, exported/debug state, or an app log that names the saved record |
| Runtime/special permission | `dumpsys package`, `appops get`, or the actual permission state in the app |
| Notification | `dumpsys notification --noredact` and visible notification when relevant |
| Service/background work | scoped logcat plus `dumpsys activity services <package>` |
| Process/task lifecycle | `dumpsys activity recents`, `dumpsys activity processes`, and scoped logcat |

Clear logcat immediately before an action only when the app/system emits a relevant log signal:

```bash
adb -s <serial> logcat -c
# perform exactly one action
adb -s <serial> logcat -d -v brief | rg '<package>|<app-tag>|ActivityManager'
```

For a background foreground-service start, inspect both the service state and ActivityManager's allow/deny reason:

```bash
adb -s <serial> shell dumpsys activity services <package>
adb -s <serial> logcat -d | rg 'Background started FGS|ForegroundServiceStart'
```

Do not claim a pass from a tap or from an absence of crashes. State the command/output or observed state that establishes each acceptance criterion.

## 5. Targeted scenarios

### Permission dialog

Dump it like any other screen. Permission-controller copy and button positions vary by Android version and OEM, so never hard-code text or coordinates. Verify the result through the app's state and `dumpsys package`/`appops` where applicable.

### Swipe away from Recents

This differs from `am force-stop`. Open Recents, find the app-card bounds in a fresh dump, swipe within the card, then verify that the task disappeared:

```bash
adb -s <serial> shell input keyevent KEYCODE_APP_SWITCH
adb -s <serial> shell dumpsys activity recents
```

Use `am force-stop` only when force-stop semantics are the requirement. Do not use it as a substitute for task removal.

### Disabled Settings toggle

Inspect the manifest declaration and device restrictions before blaming app logic. Common external causes are a missing special-access permission in the manifest, Android 13+ restricted settings for sideloaded apps, or an OEM security gate. Use `appops` only to diagnose state, and report any system-state change made during testing.

## 6. Report and cleanup

Return a concise result containing:

1. Target serial, model, Android version, build/APK installed.
2. Each scenario, action, and evidence observed.
3. Pass/fail/blocked status and the exact blocker or failure signal.
4. Any reset, permission, or system-state mutation performed.

Force-stop the app or kill an emulator only when requested or needed to leave the chosen test state. Never kill, uninstall, clear data, or alter special access on a physical device without user confirmation.
