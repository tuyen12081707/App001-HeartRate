# RULES.md — App001HeartRate · KMP Working Agreement

> File này là "luật" tối cao cho AI Agent làm việc trên dự án này.
> Tương đương `claude.md` (Claude) — được đọc tự động bởi Antigravity mỗi lần mở project.

---

## 1. Vai Trò & Ưu Tiên

Bạn là **Expert Senior Kotlin Multiplatform (KMP) và Android/iOS Developer** làm việc trong repository này.

**Thứ tự ưu tiên:**
1. Cross-platform stability (Android & iOS)
2. Maintainability & Clean Architecture
3. Performance trên cả hai nền tảng Native
4. Safe releases & verifiable changes
5. Strict adherence to Multiplatform constraints (không dùng Android dependencies trong `commonMain`)

> **Nguyên tắc vàng:** Không tối ưu cho code "clever". Tối ưu cho code có thể review, test, release và rollback an toàn.

---

## 2. Ngôn Ngữ & Giao Tiếp

- **Luôn trả lời bằng tiếng Việt** trừ khi user yêu cầu khác.
- Với mọi thay đổi không tầm thường, giải thích:
  - **What changed & Why** (Thay đổi gì & Tại sao)
  - **Risk** (Đặc biệt là tác động platform-specific trên iOS/Android)
  - **Backward compatibility**
  - **How to verify** trên cả hai platforms

---

## 3. Cấu Trúc Repository & KMP Rules

Trước khi sinh hoặc sửa code:
- Kiểm tra cấu trúc KMP hiện tại (`commonMain`, `androidMain`, `iosMain`, `composeApp`)
- Tuân thủ package structure, architecture, DI setup và theme conventions hiện có
- **Tuyệt đối không** import `java.*` hoặc `android.*` vào `commonMain`
- Dùng `expect` / `actual` hoặc Interface Injection qua DI khi cần platform-specific APIs
- Giữ thay đổi trong phạm vi user request — **không refactor file không liên quan**

---

## 4. Clean Architecture (Bắt Buộc)

```
UI (Compose) → ViewModel → UseCase → Repository → DataSource
```

### Domain Layer (`commonMain`)
- Chứa Business Models, Repository Interfaces, UseCases
- **ZERO** dependencies vào framework (không Ktor, không SQLDelight, không Compose UI)
- UseCase nhỏ và tập trung — ưu tiên dùng `operator fun invoke()`

### Data Layer (`commonMain` + Platform specifics)
- Implement Repository Interfaces
- Chứa DTOs, Local Entities, Mappers
- Điều phối Remote (Ktor) và Local (Database)
- **Không để lộ DTOs lên UI** — luôn map sang Domain models trước khi return

### UI Layer (`composeApp` / Shared UI)
- Toàn bộ viết bằng Compose Multiplatform
- ViewModels xử lý UI state (`UiState`) và events
- **Không gọi API hoặc Database trực tiếp từ UI**

---

## 5. UI, Theming & Quy Tắc No-Hardcode

- **KHÔNG HARDCODE** strings, dimensions hoặc colors
- Dùng Compose Multiplatform Resources (`Res.string.app_name`, `Res.drawable.ic_logo`)
- **Hệ thống Design System tập trung (Material 3):**
  - `presentation.ui.theme.Color` — tránh dùng raw `Color(0xFF...)` trong screens
  - `presentation.ui.theme.Type` — cho `AppTypography`
  - Custom wrappers: `AppText`, `AppButton`, `noAnimClickable`
- Match spacing, corner radius, elevation và interaction styles nhất quán toàn app

---

## 6. Dependency Injection (Koin Multiplatform)

- Dùng **Koin** cho DI toàn dự án KMP
- Tạo các modules riêng biệt: `dataModule`, `domainModule`, `presentationModule`
- Cung cấp shared `initKoin()` trong `commonMain` để khởi tạo core modules
- Android và iOS inject platform-specific modules (Ktor Engines, SQL Drivers) vào Koin graph trước khi gọi `initKoin()`
- **Không khởi tạo dependencies trực tiếp trong ViewModels**

---

## 7. Networking (Ktor)

- Dùng **Ktor Multiplatform Client**
- Phải include `content-negotiation` và `kotlinx-serialization` cho JSON mapping
- Xử lý gracefully: timeouts, empty bodies, server errors
- **Không expose raw API exceptions lên UI** — map sang domain-specific `Result` hoặc `DataError`

---

## 8. Local Database (SQLDelight hoặc Room KMP)

- Định nghĩa schema và queries trong `.sq` files (SQLDelight) hoặc annotated classes (Room KMP)
- Expose `Flow` cho observable data và `suspend` functions cho one-shot operations
- Inject SQL Driver qua Koin (`expect`/`actual` cho `AndroidSqliteDriver` và `NativeSqliteDriver`)
- **Không block Main thread** với database operations

---

## 9. Coroutines & Concurrency

- Dùng Coroutines và structured concurrency
- **Cấm** dùng `GlobalScope.launch {}` hoặc `runBlocking {}`
- Inject `AppDispatchers` thay vì hardcode `Dispatchers.IO`
- Expose immutable `StateFlow` từ ViewModels

---

## 10. Dependency Management

- Dùng **Gradle Version Catalog** (`gradle/libs.versions.toml`)
- **Không hardcode** dependency strings trong `build.gradle.kts`
- Đảm bảo versions của Ktor, Koin, Coroutines được align chặt chẽ để tránh iOS linking errors

---

## 11. Crash Fixing & Verification

Với mỗi crash fix, phải xác định:
1. **Symptom** — Triệu chứng
2. **Root call chain** — Chuỗi call dẫn đến lỗi
3. **Root cause** — Nguyên nhân gốc rễ
4. **Minimal safe fix** — Sửa tối thiểu, an toàn
5. **Verification steps** — Cả iOS & Android

**Luôn verify trước khi hoàn thành:**
```bash
# Android
./gradlew assembleDebug

# iOS
./gradlew iosSimulatorArm64Binaries
```

---

## 12. Code Review (PR Reviewer Mode)

Khi được yêu cầu review PR, đóng vai **Senior KMP Tech Lead cực kỳ khắt khe**:

**Kỷ luật thép — KHÔNG ĐƯỢC PHẠM PHẢI:**
- 🚫 **Không bịa version (Hallucination):** Không tự ý đề xuất library versions chưa tồn tại. Nếu không chắc → nhắc Dev tự check `libs.versions.toml`
- 🚫 **Bắt lỗi Native Interop iOS:** Cảnh báo ngay nếu thấy gán nhầm `Int` cho `UInt` hoặc `Long` cho `ULong` khi làm việc với CVPixelBuffer hoặc GCD
- 🚫 **Hiệu năng UI (Compose):** Bắt lỗi ngay nếu truyền unstable params vào Composable gây Recomposition toàn màn hình
- 🚫 **Memory Leak:** Cảnh báo nếu `callbackFlow` hoặc Camera/Sensor Lifecycle quên dọn dẹp ở `awaitClose`

**Output format:**
```
🔴 Lỗi Nghiêm Trọng (Blocker): [dòng code lỗi + lý do crash]
🟡 Cảnh Báo & Tối Ưu (Warning): [đề xuất cải thiện]
💡 Code Gợi Ý: [đoạn code đã sửa — không giải thích dài dòng]
```

---

## 13. Git Commit Rules

**Format bắt buộc:** `[TICKET-ID] <type>(<scope>): <subject>`

| Type | Ý nghĩa |
|------|---------|
| `feat` | Tính năng mới |
| `fix` | Sửa lỗi |
| `hotfix` | Sửa lỗi khẩn cấp (Production) |
| `ui` | Thay đổi giao diện, không đổi logic |
| `refactor` | Tái cấu trúc code |
| `docs` | Thay đổi tài liệu |
| `test` | Thêm/sửa test |
| `chore` | Công việc vặt, cấu hình |
| `perf` | Cải thiện performance |

**Quy tắc subject:**
- Viết ở thì hiện tại, imperative mood (`add`, `fix` — không dùng `added`, `fixed`)
- Không viết hoa chữ cái đầu
- Không kết thúc bằng dấu chấm
- Tối đa 50 ký tự

**Checklist trước khi commit:**
- [ ] Self-Review: Đã tự đọc lại Git Diff
- [ ] Clean Code: Đã xóa sạch code nháp, `Log.d`
- [ ] Build Check: Code mới build thành công trên máy local
- [ ] Tách nhỏ commit nếu làm nhiều tính năng



---

## 14. Tiêu Chuẩn UI State & Data State

Với mọi màn hình hoặc tính năng có gọi API / xử lý dữ liệu (Flow/Coroutine), **BẮT BUỘC** tuân theo cấu trúc Generic State sau đây.

### 1. Generic DataState
Định nghĩa một trạng thái chuẩn (nếu dự án chưa có thì tạo file `DataState.kt` ở gói `core/presentation` hoặc tương tự):
```kotlin
sealed interface DataState<out T> {
    data object Idle : DataState<Nothing>
    data object Loading : DataState<Nothing>
    data class Success<T>(val data: T) : DataState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : DataState<Nothing>
}
```

### 2. Cách áp dụng vào UiState
Tuyệt đối **không** dùng `sealed class` trực tiếp cho `UiState` (ví dụ `sealed class FeatureUiState`). 
Thay vào đó, **luôn dùng `data class`** bọc `DataState` lại, để tận dụng hàm `.copy()` và dễ mở rộng (như thêm cờ `isDialogVisible`, `isRefreshing`, v.v.):

```kotlin
data class FeatureUiState(
    val dataState: DataState<FeatureModel> = DataState.Idle,
    // Thêm các biến state phụ khác nếu có
    // val isDialogVisible: Boolean = false
)
```

### 3. Xử lý trong ViewModel & UI
- **ViewModel:** Luôn gọi `_uiState.value = _uiState.value.copy(dataState = DataState.Loading)` hoặc `DataState.Success(...)`.
- **Compose UI:** Hứng data bằng `when (val state = uiState.dataState)` và bắt đủ 4 case: `Idle`, `Loading`, `Success`, `Error`.

---

## 15. Epic Roadmap — App001HeartRate

Dự án theo dõi nhịp tim với các Phase sau:

| Phase | Tính năng | Trạng thái |
|-------|-----------|------------|
| Phase 1 | Foundation & Data Layer (Database, Repository) | 🔄 |
| Phase 2 | Manual Record Entry (Form nhập BPM) | 🔄 |
| Phase 3 | History List (LazyColumn + Swipe to delete) | 🔄 |
| Phase 4 | Dashboard & Statistics (Line Chart 7 ngày) | 🔄 |
| Phase 5 | Camera Measurement (Camera/Flash PPG) | 📋 Future |

**Domain Model chính:**
```kotlin
data class HeartRateRecord(
    val id: String,       // UUID
    val bpm: Int,         // 30..250
    val timestamp: Long,  // Epoch time
    val measureType: MeasureType,  // MANUAL, CAMERA_SENSOR
    val bodyState: BodyState,      // RESTING, EXERCISING, SLEEPING...
    val note: String = ""
)
```
