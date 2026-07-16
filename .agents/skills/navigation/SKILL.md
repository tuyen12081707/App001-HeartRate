---
name: navigation
description: Hướng dẫn AI thêm màn hình mới vào hệ thống navigation của App001HeartRate. Dự án dùng State-based Navigation tự viết (không dùng Jetpack Navigation Compose) thông qua enum Screen và `currentScreen` state trong App.kt.
---

# Navigation Skill — App001HeartRate

## Tổng Quan Hệ Thống Navigation

Dự án **KHÔNG dùng** Jetpack Navigation Compose hay NavController.
Navigation được quản lý bằng một `enum class Screen` và `var currentScreen by remember { mutableStateOf(...) }` trong `App.kt`.

### File chính: `shared/src/commonMain/kotlin/com/tdev/heartrate/App.kt`

---

## Danh Sách Màn Hình Hiện Có

```kotlin
enum class Screen {
    DISCLAIMER,          // Màn hình disclaimer — hiện khi mở app lần đầu
    HOME,                // Tab News (Health News)
    DASHBOARD,           // Tab Dashboard — Statistics
    HISTORY,             // Tab History — Heart Rate History
    ADD_RECORD,          // Thêm record thủ công
    CAMERA_MEASUREMENT,  // Đo bằng camera
    PROFILE,             // Tab Profile
    RESULT,              // Kết quả sau khi lưu
    FAILED_SCAN,         // Thất bại khi scan camera
    NEWS_DETAIL          // Chi tiết tin tức
}
```

### Bottom Bar (4 tabs hiện tại)
| Tab | Screen | Icon |
|-----|--------|------|
| Dashboard | `Screen.DASHBOARD` | `Icons.Default.Home` |
| History | `Screen.HISTORY` | `Icons.AutoMirrored.Filled.List` |
| News | `Screen.HOME` | `Icons.Default.Info` |
| Profile | `Screen.PROFILE` | `Icons.Default.Person` |

### Màn hình ẩn Bottom Bar
```kotlin
val hideBottomBarScreens = listOf(
    Screen.DISCLAIMER, Screen.CAMERA_MEASUREMENT,
    Screen.ADD_RECORD, Screen.RESULT, Screen.FAILED_SCAN, Screen.NEWS_DETAIL
)
```

---

## SOP: Thêm Màn Hình Mới (Full-screen, không có bottom bar)

### Bước 1 — Tạo Screen/ViewModel mới
Tham khảo `SOP 2` trong skill `app001heartrate` để tạo file Screen + ViewModel.

### Bước 2 — Thêm vào enum Screen
Trong `App.kt`, thêm tên mới vào enum:
```kotlin
enum class Screen {
    // ... existing screens ...
    NEW_SCREEN              // ← thêm ở đây
}
```

### Bước 3 — Thêm vào danh sách ẩn Bottom Bar (nếu cần)
```kotlin
val hideBottomBarScreens = listOf(
    Screen.DISCLAIMER, Screen.CAMERA_MEASUREMENT,
    Screen.ADD_RECORD, Screen.RESULT, Screen.FAILED_SCAN,
    Screen.NEWS_DETAIL,
    Screen.NEW_SCREEN    // ← thêm nếu màn hình này là fullscreen/modal
)
```

### Bước 4 — Thêm route vào when block
```kotlin
when (currentScreen) {
    // ... existing cases ...
    Screen.NEW_SCREEN -> {
        NewScreen(
            onNavigateBack = { currentScreen = Screen.DASHBOARD },
            onNavigateTo = { currentScreen = Screen.OTHER_SCREEN }
        )
    }
}
```

### Bước 5 — Thêm FAB trigger (nếu cần)
```kotlin
floatingActionButton = {
    if (currentScreen == Screen.HOME || currentScreen == Screen.DASHBOARD || 
        currentScreen == Screen.HISTORY || currentScreen == Screen.NEW_SCREEN) { // ← thêm
        FloatingActionButton(onClick = { currentScreen = Screen.ADD_RECORD }) {
            Icon(Icons.Default.Add, contentDescription = "Add Record")
        }
    }
}
```

---

## SOP: Thêm Tab Mới vào Bottom Bar

### Bước 1 — Thêm Screen vào enum
```kotlin
enum class Screen {
    // ...
    NEW_TAB_SCREEN
}
```

### Bước 2 — Thêm vào CustomBottomBar items list trong App.kt
```kotlin
BottomBarItem(
    title = "New Tab",
    icon = Icons.Default.Star,          // Chọn icon phù hợp từ Material Icons
    isSelected = currentScreen == Screen.NEW_TAB_SCREEN,
    onClick = { currentScreen = Screen.NEW_TAB_SCREEN }
)
```
> ⚠️ **Lưu ý**: Bottom bar chỉ nên có tối đa 5 tabs. Hiện có 4 tabs.

### Bước 3 — KHÔNG thêm vào hideBottomBarScreens (tab screens luôn hiện bottom bar)

### Bước 4 — Thêm when case cho màn hình mới

---

## SOP: Truyền Dữ Liệu Giữa Màn Hình

Vì không có NavController, dữ liệu được truyền qua `remember` state ở cấp App.kt.

### Pattern hiện tại (ví dụ NewsDetail):
```kotlin
// Khai báo state ở cấp App.kt
var selectedNewsUrl by remember { mutableStateOf("") }

// Gán trước khi navigate
onNavigateToNewsDetail = { url ->
    selectedNewsUrl = url
    currentScreen = Screen.NEWS_DETAIL
}

// Dùng trong when block
Screen.NEWS_DETAIL -> {
    NewsDetailScreen(
        url = selectedNewsUrl,
        onNavigateBack = { currentScreen = Screen.HOME }
    )
}
```

### Pattern truyền dữ liệu phức tạp hơn:
```kotlin
// Khai báo state tương tự
var selectedItemId by remember { mutableStateOf<Long?>(null) }

// Navigate với data
onNavigateToDetail = { id ->
    selectedItemId = id
    currentScreen = Screen.DETAIL_SCREEN
}

// Receive
Screen.DETAIL_SCREEN -> {
    selectedItemId?.let { id ->
        DetailScreen(
            itemId = id,
            onNavigateBack = {
                selectedItemId = null
                currentScreen = Screen.HISTORY
            }
        )
    }
}
```

---

## SOP: Back Navigation

Vì không có back stack, tự định nghĩa "back" theo logic màn hình:

```kotlin
// Trong AddRecordScreen
onNavigateBack = { currentScreen = Screen.DASHBOARD }

// Trong CameraMeasurementScreen
onNavigateBack = { currentScreen = Screen.ADD_RECORD }

// Trong NewsDetailScreen
onNavigateBack = { currentScreen = Screen.HOME }
```

**Quy tắc**: Screen nào gọi navigate tới màn hình con thì màn hình con navigate back về màn hình đó.

---

## Ví Dụ Hoàn Chỉnh: Thêm màn hình Settings

```kotlin
// 1. Tạo file: shared/presentation/settings/SettingsScreen.kt
// 2. Thêm vào enum:
enum class Screen {
    ..., SETTINGS
}

// 3. Thêm tab hoặc trigger navigation từ Profile:
// Trong ProfileScreen: onNavigateToSettings = { currentScreen = Screen.SETTINGS }

// 4. Thêm vào hideBottomBarScreens:
val hideBottomBarScreens = listOf(..., Screen.SETTINGS)

// 5. Thêm when case:
Screen.SETTINGS -> {
    SettingsScreen(onNavigateBack = { currentScreen = Screen.PROFILE })
}
```

---

## Quy Tắc Bắt Buộc

1. **LUÔN** thêm enum value vào `Screen` trước khi viết bất kỳ gì khác
2. **LUÔN** xác định rõ màn hình nào navigate back về đâu khi tạo màn hình mới
3. **KHÔNG** dùng NavController hay bất kỳ navigation library nào — chỉ dùng `currentScreen` state
4. **LUÔN** thêm màn hình fullscreen/modal vào `hideBottomBarScreens`
5. Dữ liệu truyền giữa màn hình: khai báo `remember { mutableStateOf(...) }` ở cấp `App.kt`
