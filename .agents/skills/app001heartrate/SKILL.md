---
name: app001heartrate
description: Skill chuyên biệt cho dự án App001HeartRate. Cung cấp các SOP (Standard Operating Procedures) cho AI tự động setup code API, UI, Database theo chuẩn của dự án.
---

# App001HeartRate — Skill Hướng Dẫn AI

Skill này chứa các kịch bản cụ thể, pattern code mẫu, và quy tắc bắt buộc khi làm việc với dự án **App001HeartRate (KMP)**. AI phải đọc toàn bộ skill này trước khi thực hiện bất kỳ yêu cầu nào.

---

## 📁 Cấu Trúc Dự Án (Quan Trọng)

```
App001HeartRate/
├── shared/src/
│   ├── commonMain/kotlin/com/tdev/heartrate/
│   │   ├── App.kt                          # Entry point chung
│   │   ├── shared/
│   │   │   ├── data/
│   │   │   │   ├── database/               # SQLDelight generated (KHÔNG sửa trực tiếp)
│   │   │   │   ├── mapper/                 # Mapper Entity <-> DomainModel
│   │   │   │   ├── remote/                 # DTO + API Client (Ktor)
│   │   │   │   └── repository/             # Repository Impl
│   │   │   ├── di/
│   │   │   │   └── Koin.kt                 # networkModule, domainModule, dataModule, presentationModule
│   │   │   ├── domain/
│   │   │   │   ├── model/                  # Pure Kotlin domain models
│   │   │   │   ├── repository/             # Repository interfaces
│   │   │   │   ├── sensor/                 # expect class cho Camera/Sensor
│   │   │   │   ├── usecase/                # UseCases (suspend fun)
│   │   │   │   └── utils/                  # AppDispatchers, TimeUtils (expect/actual)
│   │   │   └── presentation/
│   │   │       ├── BaseViewModel.kt        # BaseViewModel<S, I, E>
│   │   │       ├── theme/                  # Color.kt, Theme.kt (AppTheme)
│   │   │       ├── components/             # Shared reusable Composable
│   │   │       ├── home/                   # HomeScreen + HomeViewModel
│   │   │       ├── history/                # HistoryScreen + HistoryViewModel
│   │   │       ├── dashboard/              # DashboardScreen + DashboardViewModel
│   │   │       ├── add/                    # AddRecordScreen + AddRecordViewModel
│   │   │       ├── camera/                 # CameraMeasurementScreen
│   │   │       ├── result/                 # ResultScreen
│   │   │       ├── newsdetail/             # NewsDetailScreen + NewsDetailViewModel
│   │   │       ├── profile/                # ProfileScreen
│   │   │       └── disclaimer/             # DisclaimerScreen
│   │   └── sqldelight/                     # .sq schema files
│   ├── androidMain/kotlin/com/tdev/heartrate/
│   │   └── shared/
│   │       ├── di/PlatformModule.android.kt  # actual platformModule (Koin)
│   │       ├── domain/sensor/              # CameraHeartRateSensorImpl (Android)
│   │       └── domain/utils/              # actual AppDispatchers, TimeUtils
│   └── iosMain/kotlin/com/tdev/heartrate/
│       └── shared/
│           ├── di/PlatformModule.ios.kt    # actual platformModule (Koin)
│           └── domain/                     # actual sensor, utils cho iOS
├── androidApp/                             # Android-specific entry point
└── iosApp/                                 # Xcode project
```

---

## 🎨 Hệ Thống Theme & Design

### Màu sắc chuẩn (Color.kt)
- PrimaryRed = Color(0xFFE53935)
- PrimaryRedDark = Color(0xFFB71C1C)
- PrimaryRedLight = Color(0xFFFFCDD2)
- BackgroundWhite = Color(0xFFF8F9FA)
- SurfaceWhite = Color(0xFFFFFFFF)
- TextDarkCharcoal = Color(0xFF212121)
- TextGray = Color(0xFF757575)
- ErrorRed = Color(0xFFB00020)

### Áp dụng theme
- Luôn dùng `AppTheme { }` để wrap root composable.
- Dùng `MaterialTheme.colorScheme.*` thay vì hard-code màu.
- Support cả Light & Dark mode qua `isSystemInDarkTheme()`.

---

## 🏗️ SOP 1: Tạo Call API Mới

### Bước 1 — DTO
File trong `shared/data/remote/`:
```kotlin
@Serializable
data class XxxResponseDto(
    @SerialName("field_name") val fieldName: String
)
```

### Bước 2 — API Client
```kotlin
class XxxApiClient(private val httpClient: HttpClient) {
    suspend fun getXxx(): XxxResponseDto {
        return httpClient.get("https://api.example.com/endpoint").body()
    }
}
```

### Bước 3 — Domain Model
File trong `shared/domain/model/`:
```kotlin
data class Xxx(val id: Long, val name: String)
```

### Bước 4 — Repository Interface
File trong `shared/domain/repository/`:
```kotlin
interface XxxRepository {
    suspend fun getXxx(): List<Xxx>
}
```

### Bước 5 — Repository Impl
File trong `shared/data/repository/`:
```kotlin
class XxxRepositoryImpl(private val apiClient: XxxApiClient) : XxxRepository {
    override suspend fun getXxx(): List<Xxx> =
        apiClient.getXxx().items.map { it.toDomain() }
}
```

### Bước 6 — UseCase
File trong `shared/domain/usecase/`:
```kotlin
class GetXxxUseCase(private val repository: XxxRepository) {
    suspend operator fun invoke(): List<Xxx> = repository.getXxx()
}
```

### Bước 7 — Cập nhật Koin.kt
```kotlin
val networkModule = module {
    single { XxxApiClient(get()) }
}
val dataModule = module {
    single<XxxRepository> { XxxRepositoryImpl(get()) }
}
val domainModule = module {
    factory { GetXxxUseCase(get()) }
}
```

---

## 🖥️ SOP 2: Tạo Màn Hình UI Mới

### Pattern: BaseViewModel<S, I, E>
```kotlin
// State
data class XxxUiState(
    val isLoading: Boolean = false,
    val data: List<Xxx> = emptyList(),
    val error: String? = null
)

// Intent
sealed interface XxxIntent {
    data object LoadData : XxxIntent
    data class DeleteItem(val id: Long) : XxxIntent
}

// SideEffect
sealed interface XxxSideEffect {
    data class ShowToast(val message: String) : XxxSideEffect
    data object NavigateBack : XxxSideEffect
}

// ViewModel
class XxxViewModel(
    private val getXxxUseCase: GetXxxUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel<XxxUiState, XxxIntent, XxxSideEffect>(XxxUiState()) {

    init { onIntent(XxxIntent.LoadData) }

    override fun onIntent(intent: XxxIntent) {
        when (intent) {
            is XxxIntent.LoadData -> loadData()
            is XxxIntent.DeleteItem -> deleteItem(intent.id)
        }
    }

    private fun loadData() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = getXxxUseCase()
                _uiState.update { it.copy(isLoading = false, data = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
```

### Pattern: Screen Composable
```kotlin
@Composable
fun XxxScreen(
    viewModel: XxxViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is XxxSideEffect.NavigateBack -> onNavigateBack()
                is XxxSideEffect.ShowToast -> { /* show snackbar */ }
            }
        }
    }

    Scaffold(topBar = { /* TopAppBar */ }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorView(uiState.error!!) { viewModel.onIntent(XxxIntent.LoadData) }
                else -> XxxContent(uiState) { viewModel.onIntent(it) }
            }
        }
    }
}
```

### Đăng ký vào Koin
```kotlin
val presentationModule = module {
    factory { XxxViewModel(get(), get()) }
}
```

---

## 🗄️ SOP 3: Tạo/Sửa Database (SQLDelight)

### Bảng hiện có: HeartRateEntity
```sql
CREATE TABLE HeartRateEntity (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    bpm INTEGER NOT NULL,
    timestamp INTEGER NOT NULL,
    measureType TEXT AS MeasureType NOT NULL,
    bodyState TEXT AS BodyState NOT NULL,
    note TEXT
);
```

### Enum hiện có:
- MeasureType: MANUAL, CAMERA_SENSOR
- BodyState: RESTING, EXERCISING, SLEEPING, AFTER_WAKING_UP, BEFORE_BED

### Thêm bảng mới vào .sq file
```sql
CREATE TABLE NewEntity (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    value TEXT NOT NULL
);

insertNew:
INSERT INTO NewEntity(value) VALUES (?);

getAllNew:
SELECT * FROM NewEntity ORDER BY id DESC;
```

### Mapper Entity → Domain Model
File `shared/data/mapper/XxxMapper.kt`:
```kotlin
fun HeartRateEntity.toDomain(): HeartRateRecord = HeartRateRecord(
    id = this.id,
    bpm = this.bpm.toInt(),
    timestamp = this.timestamp,
    measureType = this.measureType,
    bodyState = this.bodyState,
    note = this.note
)
```

---

## 📱 SOP 4: Sensor / Camera (expect/actual)

### Pattern expect/actual
```kotlin
// commonMain
expect class XxxSensor {
    fun start()
    fun stop()
}

// androidMain
actual class XxxSensor(private val context: Context) {
    actual fun start() { /* Android */ }
    actual fun stop() { /* Android */ }
}

// iosMain
actual class XxxSensor {
    actual fun start() { /* iOS */ }
    actual fun stop() { /* iOS */ }
}
```

### Sensor hiện có
- CameraHeartRateSensor (commonMain) → CameraHeartRateSensorImpl (android + ios)
- Inject qua Koin trong PlatformModule.android.kt / .ios.kt

### AppDispatchers
- Dùng `dispatchers.io` cho network/database operations
- Dùng `dispatchers.main` cho UI updates

---

## 🔧 SOP 5: DI Structure (Koin.kt)

| Module | Nội dung |
|--------|----------|
| `networkModule` | HttpClient, API Clients |
| `dataModule` | HeartRateDatabase, Repository Impls |
| `domainModule` | UseCases, AppDispatchers |
| `presentationModule` | ViewModels |
| `platformModule` | expect/actual per platform |

- Repository → `single<Interface> { Impl(get()) }`
- UseCase → `factory { UseCase(get()) }`
- ViewModel → `factory { ViewModel(get(), get()) }`

---

## 🔍 SOP 6: Debug & Lỗi Thường Gặp

| Lỗi | Nguyên nhân | Fix |
|-----|-------------|-----|
| `Unresolved reference: android.util.Log` | Android API trong commonMain | Dùng `println()` hoặc expect/actual Logger |
| `No definition found for XxxViewModel` | Chưa đăng ký Koin | Thêm `factory { XxxViewModel(get()) }` |
| `SQLiteException: no such table` | Schema chưa migration | Tăng version schema hoặc xóa DB |
| `SerializationException` | Thiếu `@Serializable` hoặc `@SerialName` sai | Kiểm tra DTO |
| `ClassCastException` EnumColumnAdapter | Enum mới thiếu adapter | Thêm `EnumColumnAdapter()` vào HeartRateDatabase(...) |

---

## 📋 Domain Models Hiện Có

| Model | Fields |
|-------|--------|
| `HeartRateRecord` | id, bpm, timestamp, measureType, bodyState, note |
| `HeartRateStats` | (xem file HeartRateStats.kt) |
| `News` | title, description, url, urlToImage, publishedAt |
| `MeasureType` | MANUAL, CAMERA_SENSOR |
| `BodyState` | RESTING, EXERCISING, SLEEPING, AFTER_WAKING_UP, BEFORE_BED |

---

## ✅ Quy Tắc Bắt Buộc (Agent Rules)

1. **KHÔNG** dùng `android.util.*`, `android.content.*` trong `commonMain/`
2. **KHÔNG** viết code ra ngoài thư mục `App001HeartRate/`
3. **LUÔN** dùng `suspend fun` cho mọi function gọi DB hoặc network
4. **LUÔN** dùng `viewModelScope.launch(dispatchers.io)` cho background work
5. **LUÔN** đăng ký dependency mới vào đúng module trong `Koin.kt`
6. **LUÔN** tạo Domain Model thuần Kotlin, không mix với DTO hoặc Entity
7. **TỰ ĐỘNG** thực hiện tất cả các bước SOP mà không cần hỏi lại
8. **LUÔN** kế thừa `BaseViewModel<S, I, E>` khi tạo ViewModel mới
9. **KHÔNG** hard-code màu hex — dùng `MaterialTheme.colorScheme.*` hoặc constants từ `Color.kt`
10. SQLDelight query: đặt tên theo format `camelCase:` (ví dụ `getRecordById:`)
