---
name: ui-components
description: Thư viện component UI có sẵn trong App001HeartRate, hướng dẫn tái sử dụng và tạo component mới theo chuẩn dự án. Bao gồm AnimatedPrimaryButton, CustomBottomBar, WheelNumberPicker, BodyStateTile, StatRowItem, v.v.
---

# UI Components Skill — App001HeartRate

## Tổng Quan

Dự án dùng **Compose Multiplatform** cho cả Android và iOS.
Tất cả component chia sẻ nằm trong: `shared/src/commonMain/kotlin/com/tdev/heartrate/shared/presentation/`

---

## 📦 Component Có Sẵn

### 1. `AnimatedPrimaryButton`
**File**: `components/AnimatedPrimaryButton.kt`
**Mô tả**: Button chính với hiệu ứng scale spring khi nhấn.

```kotlin
AnimatedPrimaryButton(
    onClick = { /* action */ },
    modifier = Modifier.fillMaxWidth().height(56.dp),
    enabled = isFormValid,
    containerColor = MaterialTheme.colorScheme.primary, // optional
    contentColor = MaterialTheme.colorScheme.onPrimary  // optional
) {
    Text("Button Text", style = MaterialTheme.typography.titleMedium)
}
```
**Tính năng**: Spring bounce animation, disabled state (50% alpha), hỗ trợ loading spinner bên trong.

---

### 2. `CustomBottomBar`
**File**: `components/CustomBottomBar.kt`
**Mô tả**: Bottom navigation bar với animated selection, dùng `BottomBarItem` data class.

```kotlin
// Data class
data class BottomBarItem(
    val title: String,
    val icon: ImageVector,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

// Dùng
CustomBottomBar(
    items = listOf(
        BottomBarItem(
            title = "Dashboard",
            icon = Icons.Default.Home,
            isSelected = currentScreen == Screen.DASHBOARD,
            onClick = { currentScreen = Screen.DASHBOARD }
        )
    )
)
```
**Tính năng**: Animated color transition, scale animation khi selected, text label chỉ hiện khi selected (AnimatedVisibility), rounded pill shape.

---

### 3. `WheelNumberPicker`
**File**: `add/AddRecordScreen.kt` (inline)
**Mô tả**: Picker cuộn dọc chọn số BPM kiểu iOS wheel, range 40..220.

```kotlin
WheelNumberPicker(
    selectedValue = tempBpm,
    onValueChange = { newBpm -> tempBpm = newBpm },
    range = 40..220   // optional, default 40..220
)
```
**Tính năng**: Snap fling behavior, scale + alpha animation cho item selected, highlight background cho item trung tâm.

---

### 4. `BodyStateTile`
**File**: `add/AddRecordScreen.kt` (inline)
**Mô tả**: Tile hình vuông 70dp để chọn BodyState với emoji + label.

```kotlin
BodyStateTile(
    state = BodyState.RESTING,
    emoji = "😴",
    isSelected = uiState.bodyState == BodyState.RESTING,
    onClick = { viewModel.onIntent(AddRecordIntent.UpdateBodyState(BodyState.RESTING)) }
)
```
**Emoji mapping**:
- `RESTING` → 😴
- `EXERCISING` → 🏃
- `SLEEPING` → 🛌
- `AFTER_WAKING_UP` → 🌅
- `BEFORE_BED` → 🌙

---

### 5. `StatRowItem`
**File**: `dashboard/DashboardScreen.kt` (inline)
**Mô tả**: Card nhỏ hiển thị 1 stat (title + value + color).

```kotlin
StatRowItem(
    title = "Highest",
    value = "120",
    color = Color(0xFFEF5350),  // Red
    modifier = Modifier.weight(1f)
)
```

---

### 6. Loading / Error / Empty Views
**Pattern chuẩn** dùng trong nhiều màn hình:

```kotlin
// Loading
Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
}

// Error với retry
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onRetry) {
        Icon(Icons.Rounded.Refresh, contentDescription = "Retry")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Retry")
    }
}

// Empty state
Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(
        text = "No records yet",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge
    )
}
```

---

## 🎨 Patterns UI Chuẩn

### TopAppBar chuẩn
```kotlin
TopAppBar(
    title = { Text("Screen Title", color = Color.White) },
    navigationIcon = {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
)
```

### Card chuẩn
```kotlin
Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
) {
    Column(modifier = Modifier.padding(24.dp)) {
        // content
    }
}
```

### Glassmorphism Card (dùng trong HomeScreen)
```kotlin
Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
) { /* content */ }
```

### Gradient Header (dùng trong DashboardScreen)
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            ),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        )
        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
) { /* header content */ }
```

### OutlinedTextField chuẩn (dùng trong AddRecordScreen)
```kotlin
OutlinedTextField(
    value = value,
    onValueChange = { onChange(it) },
    label = { Text("Label") },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
    shape = RoundedCornerShape(16.dp),
    colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Transparent,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedContainerColor = MaterialTheme.colorScheme.surface
    )
)
```

---

## 📐 Spacing & Size Chuẩn

| Element | Giá trị |
|---------|---------|
| Horizontal padding của content | `24.dp` |
| Card corner radius lớn | `24.dp` hoặc `32.dp` |
| Card corner radius nhỏ | `16.dp` |
| Button corner radius | `100` (pill) |
| FAB size | Default Material FAB |
| Bottom bar padding | `horizontal: 24.dp, vertical: 16.dp` |
| Spacing giữa sections | `24.dp` |
| Spacing nhỏ (inline) | `8.dp` hoặc `12.dp` |

---

## 🖼️ Resources (Compose Multiplatform)

### String resources
File: `commonMain/composeResources/values/strings.xml`

**Cách dùng**:
```kotlin
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.string_key_name
import org.jetbrains.compose.resources.stringResource

// Trong composable
Text(text = stringResource(Res.string.string_key_name))
```

**String keys hiện có**:
- `tab_dashboard`, `tab_history`, `bpm_unit`
- `disclaimer_title`, `disclaimer_content`, `disclaimer_agree`
- `history_title`, `history_empty`, `history_delete`
- `add_record_title`, `add_record_bpm_label`, `add_record_body_state`, `add_record_note_label`, `add_record_save_button`
- `dashboard_title`, `dashboard_average`, `dashboard_count`, `dashboard_highest`, `dashboard_lowest`
- `camera_measure_button`, `camera_title`, `camera_instruction`, `camera_measuring`, `camera_completed`
- `failed_scan_title`, `failed_scan_description`, `failed_scan_try_again`, `failed_scan_go_home`

**Thêm string mới**:
1. Thêm vào `strings.xml`: `<string name="new_key">Value</string>`
2. Rebuild project để generate `Res.string.new_key`
3. Dùng: `stringResource(Res.string.new_key)`

### Drawable resources
File: `commonMain/composeResources/drawable/`
Dùng: `painterResource(Res.drawable.image_name)`

---

## SOP: Tạo Component Mới Cho `components/`

Khi một pattern UI được tái sử dụng ≥ 2 lần, hãy extract thành component:

```kotlin
// File: components/XxxComponent.kt
package com.tdev.heartrate.shared.presentation.components

@Composable
fun XxxComponent(
    // required params trước
    title: String,
    onClick: () -> Unit,
    // optional params có default value sau
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    // Implementation dùng MaterialTheme, không hard-code màu
}
```

**Quy tắc component**:
1. Luôn có `modifier: Modifier = Modifier` parameter
2. Luôn dùng `MaterialTheme.colorScheme.*` thay vì Color literal
3. Nếu component có state nội bộ, dùng `remember { }` bên trong
4. Preview không cần thêm vào commonMain (không support `@Preview` trong KMP)

---

## 🎬 Animation Chuẩn

### Scale bounce (dùng trong buttons, bottom bar)
```kotlin
val scale by animateFloatAsState(
    targetValue = if (isSelected) 1.05f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "scale"
)
Modifier.scale(scale)
```

### Color transition
```kotlin
val color by animateColorAsState(
    targetValue = if (active) MaterialTheme.colorScheme.primary else Color.Gray,
    label = "color"
)
```

### Crossfade giữa loading/content (dùng trong DashboardScreen)
```kotlin
Crossfade(
    targetState = isLoading,
    animationSpec = tween(500),
    label = "loading_crossfade"
) { loading ->
    if (loading) LoadingView() else ContentView()
}
```

### AnimatedVisibility (show/hide)
```kotlin
AnimatedVisibility(visible = isSelected) {
    Text("Label visible when selected")
}
```

---

## Quy Tắc Bắt Buộc

1. **KHÔNG** tạo lại component đã có — kiểm tra danh sách trên trước
2. **LUÔN** dùng `AnimatedPrimaryButton` thay vì `Button` cho action chính
3. **KHÔNG** hard-code size font — dùng `MaterialTheme.typography.*`
4. **LUÔN** thêm string mới vào `strings.xml` thay vì hard-code text tiếng Anh
5. **LUÔN** thêm `modifier: Modifier = Modifier` vào mọi composable
6. Spacing: dùng `Spacer(modifier = Modifier.height(X.dp))` thay vì `padding` để tách sections
