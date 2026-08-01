---
name: project-context
description: "Bản đồ sống" của dự án App001HeartRate — toàn bộ code thực tế đã tồn tại, class names, function signatures, data flow, và các recipe copy-paste sẵn. Đọc skill này để hiểu ngay context mà không cần khám phá lại codebase. Kích hoạt khi user yêu cầu bất kỳ task nào liên quan đến dự án."
---

# Project Context — App001HeartRate (Living Summary)

> Đây là "bản đồ sống" của dự án. Chứa toàn bộ code thực tế đã tồn tại để AI generate code mới chính xác ngay lập tức mà không cần đọc lại file.
> **Cập nhật skill này mỗi khi thêm class/file mới vào dự án.**

---

## ⚙️ Stack & Package

| Item | Giá trị |
|------|---------|
| Base package | `com.tdev.heartrate` |
| Shared module | `com.tdev.heartrate.shared` |
| DB package | `com.tdev.heartrate.shared.data.database` |
| DB class name | `HeartRateDatabase` |
| DB queries accessor | `database.heartRateDatabaseQueries` |
| API base | Ktor `HttpClient` với `ContentNegotiation + kotlinx.json` |
| DI | Koin (`networkModule`, `dataModule`, `domainModule`, `presentationModule`, `platformModule`) |
| Navigation | Typed state-based routes via `AppNavigator`/`AppRoute` in `App.kt` |

### Android Compose Resources workaround (AGP 9)

`shared` dùng `com.android.kotlin.multiplatform.library` cùng AGP 9. Compose Resources
vẫn generate `strings.commonMain.cvr` nhưng AGP không tự package nó vào APK, gây crash:
`MissingResourceException: composeResources/.../strings.commonMain.cvr`.

- Trong `shared/build.gradle.kts`, giữ Android KMP target
  `com.android.kotlin.multiplatform.library` và `androidResources.enable = true`.
- Trong `androidApp/build.gradle.kts`, task `copySharedComposeResourcesToJavaResources` copy
  `shared/build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources`
  vào Java-resources root `composeResources/app001heartrate.shared.generated.resources/`.
  Compose Resources 1.6.11 dùng `ClassLoader.getResourceAsStream(path)` cho strings,
  vì vậy không được đặt file dưới `assets/`.
- Task được đăng ký qua `androidComponents.onVariants { variant.sources.resources
  .addGeneratedSourceDirectory(...) }`; không dùng `sourceSets.assets.srcDir(Provider)`
  vì AGP 9 cấm Provider ở SourceSet API.
- Task dùng `FileSystemOperations` được inject; không gọi `Task.project` trong
  `@TaskAction` để tương thích Gradle configuration cache.
- Sau khi đổi resources/build logic, chạy `./gradlew clean :androidApp:assembleDebug`
  rồi xác nhận APK có `composeResources/.../strings.commonMain.cvr` ở root, không
  có prefix `assets/`, bằng `unzip -l`.

---

## 🗄️ Database — Hiện Trạng Thực Tế

### Schema (HeartRateDatabase.sq)
```sql
CREATE TABLE HeartRateEntity (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    bpm INTEGER NOT NULL,
    timestamp INTEGER NOT NULL,
    measureType TEXT AS MeasureType NOT NULL,
    bodyState TEXT AS BodyState NOT NULL,
    note TEXT
);

CREATE TABLE BloodPressureEntity (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    systolic INTEGER NOT NULL,
    diastolic INTEGER NOT NULL,
    pulse INTEGER NOT NULL,
    timestamp INTEGER NOT NULL,
    note TEXT
);

CREATE TABLE AppMetadataEntity (
    key TEXT NOT NULL PRIMARY KEY,
    value TEXT NOT NULL
);
```

### Queries đã có
| Query name | SQL |
|-----------|-----|
| `insertRecord` | `INSERT INTO HeartRateEntity(bpm, timestamp, measureType, bodyState, note) VALUES (?, ?, ?, ?, ?)` |
| `deleteRecord` | `DELETE FROM HeartRateEntity WHERE id = ?` |
| `getAllRecords` | `SELECT * FROM HeartRateEntity ORDER BY timestamp DESC` |
| `getRecordById` | `SELECT * FROM HeartRateEntity WHERE id = ?` |
| `lastInsertRowId` | `SELECT last_insert_rowid()` immediately after an insert on the same connection |
| `getAverageBpm` | `SELECT AVG(bpm) FROM HeartRateEntity` |
| `getMetadata` | `SELECT value FROM AppMetadataEntity WHERE key = ?` |
| `upsertMetadata` | SQLite-compatible `INSERT OR REPLACE` by metadata key |
| `insertBloodPressureRecord` | `INSERT INTO BloodPressureEntity(systolic, diastolic, pulse, timestamp, note) VALUES (?, ?, ?, ?, ?)` |
| `getAllBloodPressureRecords` | `SELECT * FROM BloodPressureEntity ORDER BY timestamp DESC` |

**Migration hiện có**: `1.sqm` tạo `BloodPressureEntity`; `2.sqm` tạo
`AppMetadataEntity` cho database đã cài trước đó.

---

## 🌐 API Layer — Hiện Trạng Thực Tế

### NewsApiClient (`data/remote/NewsApiClient.kt`)
```kotlin
class NewsApiClient(private val httpClient: HttpClient) {
    suspend fun getHealthNews(): NewsResponseDto        // GET https://saurav.tech/NewsAPI/top-headlines/category/health/us.json
    suspend fun getNewsDetail(url: String): NewsDetailDto   // mock, delay 1s
}
```

### DTOs (`data/remote/NewsDto.kt`)
```kotlin
@Serializable data class NewsResponseDto(val status: String, val totalResults: Int, val articles: List<NewsDto>)
@Serializable data class NewsDto(val title: String?, val description: String?, val urlToImage: String?, val url: String?, val publishedAt: String?)
@Serializable data class NewsDetailDto(val url: String?, val content: String?)

// Extension mappers (đã có)
fun NewsDto.toDomain(): News
fun NewsDetailDto.toDomain(): NewsDetail
```

---

## 🏛️ Domain Layer — Hiện Trạng Thực Tế

### Models (`domain/model/`)
```kotlin
data class HeartRateRecord(val id: Long = 0, val bpm: Int, val timestamp: Long, val measureType: MeasureType, val bodyState: BodyState, val note: String? = null)
data class HeartRateStats(val averageBpm: Int = 0, val maxBpm: Int = 0, val minBpm: Int = 0, val totalRecords: Int = 0)
data class DashboardPoint(val dayStartMillis: Long, val averageBpm: Int, val recordCount: Int)
data class DashboardData(val latest: HeartRateRecord?, val averageBpm: Int, val minBpm: Int, val maxBpm: Int, val totalRecords: Int, val points: List<DashboardPoint>)
data class BloodPressureRecord(val id: Long = 0, val systolic: Int, val diastolic: Int, val pulse: Int, val timestamp: Long, val note: String? = null)
data class News(val title: String, val description: String, val urlToImage: String?, val url: String, val publishedAt: String)
data class NewsDetail(val url: String, val content: String)  // kiểm tra file
enum class MeasureType { MANUAL, CAMERA_SENSOR }
enum class BodyState { RESTING, EXERCISING, SLEEPING, AFTER_WAKING_UP, BEFORE_BED }
enum class BloodPressureLevel { HYPOTENSION, NORMAL, HYPERTENSION_STAGE_1, HYPERTENSION_STAGE_2, HYPERTENSIVE_CRISIS }

fun BloodPressureRecord.level(): BloodPressureLevel
fun classifyBloodPressure(systolic: Int, diastolic: Int): BloodPressureLevel
object BloodPressureThresholds // ngưỡng phân loại duy nhất dùng bởi domain + UI
object BloodPressureInputConstraints // range nhập Systolic/Diastolic/Pulse
```

### Repository Interfaces (`domain/repository/`)
```kotlin
// HeartRateRepository.kt
interface HeartRateRepository {
    suspend fun insertRecord(record: HeartRateRecord): Long
    suspend fun getRecordById(id: Long): HeartRateRecord?
    suspend fun deleteRecord(id: Long)
    fun getAllRecords(): Flow<List<HeartRateRecord>>
    suspend fun getAverageBpm(): Double
}

interface AppMetadataRepository {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
}

interface BloodPressureRepository {
    suspend fun insertRecord(record: BloodPressureRecord)
    fun getAllRecords(): Flow<List<BloodPressureRecord>>
}

// NewsRepository.kt
interface NewsRepository {
    suspend fun getHealthNews(): List<News>
    suspend fun getNewsDetail(url: String): NewsDetail   // kiểm tra file thực tế
}
```

### Use Cases (`domain/usecase/`) — Đã có
| Class | Constructor | invoke() signature |
|-------|------------|-------------------|
| `AddHeartRateRecordUseCase` | `(HeartRateRepository, Clock)` | `suspend invoke(bpm, measureType, bodyState, note, timestamp: Long? = null): Long` |
| `GetHeartRateRecordUseCase` | `(HeartRateRepository)` | `suspend invoke(id: Long): HeartRateRecord?` |
| `GetDashboardDataUseCase` | `(HeartRateRepository, Clock)` | `invoke(): Flow<DashboardData>`; current deterministic epoch-day bucket plus six prior buckets |
| `SeedDemoHeartRateUseCase` | `(HeartRateRepository, AppMetadataRepository, Clock)` | `suspend invoke(): Boolean`; inserts seven fixed manual records once using `demo_seed_v1` |
| `AddBloodPressureRecordUseCase` | `(BloodPressureRepository)` | `suspend invoke(systolic: Int, diastolic: Int, pulse: Int, timestamp: Long = now, note: String? = null)` |
| `GetHeartRateHistoryUseCase` | `(HeartRateRepository)` | `invoke(): Flow<List<HeartRateRecord>>` |
| `DeleteHeartRateRecordUseCase` | `(HeartRateRepository)` | `suspend invoke(id: Long)` |
| `GetHeartRateStatsUseCase` | `(HeartRateRepository)` | `invoke(): Flow<HeartRateStats>` |
| `GetNewsUseCase` | `(NewsRepository)` | `suspend invoke(): List<News>` |
| `GetNewsDetailUseCase` | `(NewsRepository)` | kiểm tra file |

### Startup consent/demo coordination (2026-08-01)

- `AppConfig(demoDataEnabled: Boolean)` controls whether debug/demo startup seeding is enabled.
- `StartupData(consentAccepted: Boolean)` is the startup result consumed by presentation/navigation.
- `GetDisclaimerStatusUseCase` and `AcceptDisclaimerUseCase` persist/read the
  `disclaimer_accepted` metadata key (`"true"` means accepted).
- `AppStartupCoordinator.start(): Flow<DataState<StartupData>>` emits Loading, then
  Success with consent status after optional `SeedDemoHeartRateUseCase` execution,
  or Error carrying the original failure. Demo seeding is skipped when
  `AppConfig.demoDataEnabled` is false.

---

## 🎭 Presentation Layer — Hiện Trạng Thực Tế

### BaseViewModel pattern (phải kế thừa class này)
```kotlin
abstract class BaseViewModel<S, I, E>(initialState: S) : ViewModel() {
    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()
    protected val _sideEffect = MutableSharedFlow<E>()
    val sideEffect: SharedFlow<E> = _sideEffect.asSharedFlow()
    abstract fun onIntent(intent: I)
    protected fun emitSideEffect(effect: E)   // gọi trong viewModelScope.launch
}
```

### ViewModels đã có
| ViewModel | UiState | Intent | SideEffect | Constructor |
|-----------|---------|--------|------------|------------|
| `HomeViewModel` | `HomeUiState` (Loading/Success/Error) | — | — | `(GetNewsUseCase, GetHeartRateHistoryUseCase)` |
| `DashboardViewModel` | `DashboardUiState(data: DataState<DashboardData>)` | `Unit` | `Unit` | `(GetDashboardDataUseCase)` |
| `HistoryViewModel` | `HistoryUiState(data: DataState<List<HeartRateRecord>>, deleteState: DataState<Long>)` | `HistoryIntent.DeleteRecord(id)` | `Unit` | `(GetHeartRateHistoryUseCase, DeleteHeartRateRecordUseCase)` |
| `AddRecordViewModel` | `AddRecordUiState(bpm, bodyState, note, saveState: DataState<Long>, fieldErrors)` | `UpdateBpm/UpdateBodyState/UpdateNote/SaveRecord/ClearError` | `NavigateBack/NavigateToResult(recordId)/ShowSnackbar(message)` | `(AddHeartRateRecordUseCase)` |
| `BloodPressureViewModel` | `BloodPressureUiState(systolic, diastolic, pulse, timestamp, note, isLoading, errorMessage)` + derived `level` | `UpdateSystolic/UpdateDiastolic/UpdatePulse/UpdateTimestamp/UpdateNote/SaveRecord` | `NavigateBack/ShowError(message)` | `(AddBloodPressureRecordUseCase)` |
| `NewsDetailViewModel` | — | — | — | `(GetNewsDetailUseCase)` |

### Screens đã có & navigation params
| Screen | File | onNavigateXxx params |
|--------|------|---------------------|
| `DisclaimerScreen` | `disclaimer/` | `onAgree: () -> Unit` |
| `HomeScreen` | `home/` | `onNavigateToAddRecord`, `onNavigateToNewsDetail(url)` |
| `DashboardScreen` | `dashboard/` | `viewModel`, `onNavigateToAddRecord`, `onNavigateToBloodPressure`; Home UI theo Figma node `27:8823` |
| `HistoryScreen` | `history/` | viewModel param trực tiếp |
| `AddRecordScreen` | `add/` | `onNavigateBack`, `onOpenCamera` |
| `BloodPressureScreen` | `bloodpressure/` | `viewModel`, `onNavigateBack`; Figma node `33:2705` |
| `CameraMeasurementScreen` | `camera/` | `onNavigateBack`, `onMeasurementCompleted(bpm)`, `onMeasurementFailed` |
| `ProfileScreen` | `profile/` | không có |
| `ResultScreen` | `result/` | `bpm: Int`, `bodyState: String`, `onGoHome`, `onMeasureAgain` |
| `FailedScanScreen` | `camera/` | `onTryAgain`, `onGoHome` |
| `NewsDetailScreen` | `newsdetail/` | `url: String`, `onNavigateBack` |

Typed navigation is defined in `presentation/navigation/`: `MainTab` (Dashboard,
History, News, Profile), `AppRoute` (Disclaimer, Main(tab), AddHeartRate,
Result(recordId), plus retained NewsDetail/BloodPressure/Camera/FailedScan routes),
and `AppNavigator` (StateFlow route with stack-based navigate/back). `ResultViewModel`
loads a persisted `recordId` and exposes `ResultUiState(data: DataState<HeartRateRecord>)`.

**Home UI assets** (`commonMain/composeResources/drawable/`):
`home_heart_wave.png`, `home_heart.png`, `home_blood_pressure.png`,
`home_blood_sugar.png`. Card Heart rate mở `ADD_RECORD`; card Blood Pressure mở
`BLOOD_PRESSURE_RECORD`; Blood sugar hiện chỉ là UI placeholder.

---

## 🔧 DI — Koin Modules (Koin.kt thực tế)

```kotlin
val networkModule = module {
    single { HttpClient { install(ContentNegotiation) { json(Json { ignoreUnknownKeys=true; isLenient=true }) } } }
    single { NewsApiClient(get()) }
    // ← THÊM ApiClient mới ở đây
}

val dataModule = module {
    single { HeartRateDatabase(driver=get(), HeartRateEntityAdapter=HeartRateEntity.Adapter(measureTypeAdapter=EnumColumnAdapter(), bodyStateAdapter=EnumColumnAdapter())) }
    single<HeartRateRepository> { HeartRateRepositoryImpl(get(), get()) }
    single<AppMetadataRepository> { AppMetadataRepositoryImpl(get()) }
    single<BloodPressureRepository> { BloodPressureRepositoryImpl(get(), get()) }
    single<NewsRepository> { NewsRepositoryImpl(get()) }
    // ← THÊM Repository mới ở đây
}

val domainModule = module {
    factory { AddHeartRateRecordUseCase(get(), get()) }
    factory { AddBloodPressureRecordUseCase(get()) }
    factory { GetHeartRateHistoryUseCase(get()) }
    factory { GetHeartRateRecordUseCase(get()) }
    factory { DeleteHeartRateRecordUseCase(get()) }
    factory { GetHeartRateStatsUseCase(get()) }
    factory { GetDashboardDataUseCase(get(), get()) }
    factory { SeedDemoHeartRateUseCase(get(), get(), get()) }
    factory { GetNewsUseCase(get()) }
    factory { GetNewsDetailUseCase(get()) }
    single<Clock> { SystemClock }
    single { provideAppDispatchers() }
    // ← THÊM UseCase mới ở đây
}

val presentationModule = module {
    factory { AppStartupCoordinator(get(), get(), get()) }
    factory { HistoryViewModel(get(), get()) }
    factory { AddRecordViewModel(get()) }
    factory { BloodPressureViewModel(get()) }
    factory { DashboardViewModel(get()) }
    factory { HomeViewModel(get(), get()) }
    factory { NewsDetailViewModel(get()) }
    // ← THÊM ViewModel mới ở đây
}

expect val platformModule: Module   // Android: AndroidSqliteDriver + CameraHeartRateSensorImpl
```

### Deterministic time and dashboard data (2026-08-01)

- `domain/utils/Clock.kt` defines `Clock.nowMillis()` and `SystemClock`, which delegates
  to the existing cross-platform `getCurrentTimeMillis()` expect/actual function.
- Dashboard aggregation is offline-first over `HeartRateRepository.getAllRecords()`.
  It derives up to one non-empty `DashboardPoint` per device-local calendar-day bucket
  using an injected `kotlinx.datetime.TimeZone`, ordered oldest-to-newest, and excludes
  records outside the injected clock's current seven-day window (including future records).
- `DemoSeedRepository.seedIfAbsent(...)` coordinates the marker check, all seven inserts,
  and marker write in one SQLDelight transaction, so concurrent calls serialize and an
  insert/marker failure rolls back the complete seed.

---

## 🗺️ Navigation — Typed routes (App.kt)

```kotlin
enum class Screen {
    DISCLAIMER, HOME, DASHBOARD, HISTORY,
    ADD_RECORD, BLOOD_PRESSURE_RECORD, CAMERA_MEASUREMENT, PROFILE,
    RESULT, FAILED_SCAN, NEWS_DETAIL
    // ← THÊM màn hình mới ở đây
}
```

**Bottom bar tabs (theo thứ tự)**:
1. `DASHBOARD` — icon: `Icons.Default.Home`
2. `HISTORY` — icon: `Icons.AutoMirrored.Filled.List`
3. `HOME` (News) — icon: `Icons.Default.Info`
4. `PROFILE` — icon: `Icons.Default.Person`

**Screens ẩn bottom bar**: `DISCLAIMER, CAMERA_MEASUREMENT, ADD_RECORD, BLOOD_PRESSURE_RECORD, RESULT, FAILED_SCAN, NEWS_DETAIL`

**Flow huyết áp**: `DashboardScreen` card Blood Pressure →
`Screen.BLOOD_PRESSURE_RECORD` → `BloodPressureScreen`; Back hoặc Save thành công
quay về `Screen.DASHBOARD`.

### Lịch sử triển khai — Blood Pressure SP005 (2026-07-30)

Hoàn thành đầy đủ node Figma `33:2705`: domain model/level classification,
repository/mapper/use case, SQLDelight entity/query/migration `1.sqm`,
`BloodPressureViewModel`, `BloodPressureScreen`, date/time wheel dialog, live
classification từ domain thresholds, Koin, navigation Dashboard và unit tests.
Timestamp người dùng chọn được truyền xuyên ViewModel → use case → repository.
Validation đã pass:
`:shared:compileAndroidMain`, `:shared:testAndroidHostTest`,
`:shared:compileKotlinIosSimulatorArm64`, `:androidApp:assembleDebug`.
APK debug: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

**Shared state trong App.kt**:
```kotlin
var currentScreen by remember { mutableStateOf(Screen.DISCLAIMER) }
var prefilledBpm by remember { mutableStateOf<String?>(null) }      // Camera → AddRecord
var lastSavedBpm by remember { mutableStateOf(0) }                  // AddRecord → Result
var lastSavedBodyState by remember { mutableStateOf("") }           // AddRecord → Result
var selectedNewsUrl by remember { mutableStateOf("") }              // Home → NewsDetail
// ← THÊM state mới để truyền data giữa màn hình ở đây
```

---

## 📋 RECIPE: Thêm API mới cho một tính năng

> Ví dụ: "Tôi muốn call API lấy lịch sử nhịp tim từ server"

### Bước 1 — DTO (`data/remote/HeartRateDto.kt`)
```kotlin
package com.tdev.heartrate.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartRateHistoryResponseDto(
    @SerialName("data") val data: List<HeartRateItemDto>
)

@Serializable
data class HeartRateItemDto(
    @SerialName("id") val id: Long,
    @SerialName("bpm") val bpm: Int,
    @SerialName("timestamp") val timestamp: Long
)

// Mapper
fun HeartRateItemDto.toDomain() = HeartRateRecord(
    id = id, bpm = bpm, timestamp = timestamp,
    measureType = MeasureType.MANUAL, bodyState = BodyState.RESTING
)
```

### Bước 2 — Thêm vào ApiClient hiện có HOẶC tạo mới
```kotlin
// Thêm vào NewsApiClient.kt hoặc tạo HeartRateApiClient.kt mới:
class HeartRateApiClient(private val httpClient: HttpClient) {
    suspend fun getRemoteHistory(): HeartRateHistoryResponseDto {
        return httpClient.get("https://api.example.com/heartrate/history").body()
    }
}
```

### Bước 3 — Thêm function vào HeartRateRepository interface
```kotlin
interface HeartRateRepository {
    // ... existing ...
    suspend fun getRemoteHistory(): List<HeartRateRecord>   // ← thêm
}
```

### Bước 4 — Implement trong HeartRateRepositoryImpl
```kotlin
class HeartRateRepositoryImpl(
    private val database: HeartRateDatabase,
    private val dispatchers: AppDispatchers,
    private val apiClient: HeartRateApiClient   // ← thêm dependency
) : HeartRateRepository {
    // ... existing ...
    override suspend fun getRemoteHistory(): List<HeartRateRecord> {
        return apiClient.getRemoteHistory().data.map { it.toDomain() }
    }
}
```

### Bước 5 — Tạo UseCase
```kotlin
class GetRemoteHeartRateHistoryUseCase(private val repository: HeartRateRepository) {
    suspend operator fun invoke(): List<HeartRateRecord> = repository.getRemoteHistory()
}
```

### Bước 6 — Cập nhật Koin.kt
```kotlin
val networkModule = module {
    // ...
    single { HeartRateApiClient(get()) }
}
val dataModule = module {
    // Cập nhật HeartRateRepositoryImpl constructor — thêm get() mới:
    single<HeartRateRepository> { HeartRateRepositoryImpl(get(), get(), get()) }
}
val domainModule = module {
    // ...
    factory { GetRemoteHeartRateHistoryUseCase(get()) }
}
```

---

## 📋 RECIPE: Thêm màn hình mới (ví dụ: Settings)

```kotlin
// 1. Tạo file: shared/presentation/settings/SettingsScreen.kt + SettingsViewModel.kt

// 2. App.kt — thêm vào enum:
enum class Screen { ..., SETTINGS }

// 3. App.kt — thêm vào hideBottomBarScreens:
val hideBottomBarScreens = listOf(..., Screen.SETTINGS)

// 4. App.kt — thêm when case:
Screen.SETTINGS -> {
    SettingsScreen(onNavigateBack = { currentScreen = Screen.PROFILE })
}

// 5. Trigger navigate từ ProfileScreen (ví dụ):
ProfileScreen(onNavigateToSettings = { currentScreen = Screen.SETTINGS })

// 6. Koin.kt — presentationModule:
factory { SettingsViewModel(get()) }
```

---

## 📋 RECIPE: Thêm query vào database hiện có

```sql
-- 1. Thêm vào HeartRateDatabase.sq:
getRecordsByBodyState:
SELECT * FROM HeartRateEntity WHERE bodyState = ? ORDER BY timestamp DESC;

getRecordsBetween:
SELECT * FROM HeartRateEntity WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC;
```

```kotlin
// 2. Dùng trong HeartRateRepositoryImpl:
override fun getRecordsByBodyState(state: BodyState): Flow<List<HeartRateRecord>> {
    return database.heartRateDatabaseQueries.getRecordsByBodyState(state)
        .asFlow().mapToList(dispatchers.io)
        .map { list -> list.map { it.toDomainModel() } }
}
```

---

## 📋 RECIPE: ViewModel mới — copy pattern từ HistoryViewModel

```kotlin
// shared/presentation/xxx/XxxViewModel.kt
package com.tdev.heartrate.shared.presentation.xxx

import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class XxxUiState(
    val isLoading: Boolean = true,
    val records: List<HeartRateRecord> = emptyList(),
    val error: String? = null
)

sealed interface XxxIntent {
    data class DeleteRecord(val id: Long) : XxxIntent
    data object Refresh : XxxIntent
}

sealed interface XxxSideEffect {
    data class ShowToast(val msg: String) : XxxSideEffect
}

class XxxViewModel(
    private val getHeartRateHistoryUseCase: GetHeartRateHistoryUseCase,  // inject use case cần
    private val deleteHeartRateRecordUseCase: DeleteHeartRateRecordUseCase
) : BaseViewModel<XxxUiState, XxxIntent, XxxSideEffect>(XxxUiState()) {

    init { loadData() }

    override fun onIntent(intent: XxxIntent) {
        when (intent) {
            is XxxIntent.DeleteRecord -> viewModelScope.launch {
                deleteHeartRateRecordUseCase(intent.id)
                emitSideEffect(XxxSideEffect.ShowToast("Deleted"))
            }
            XxxIntent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            getHeartRateHistoryUseCase().collect { records ->
                _uiState.value = XxxUiState(isLoading = false, records = records)
            }
        }
    }
}
```

---

## ⚠️ Điều Chỉnh Tự Động Khi Generate Code

Khi generate code mới, AI **PHẢI**:
1. Kiểm tra bảng **"Use Cases đã có"** — nếu đã có `GetHeartRateHistoryUseCase` thì KHÔNG tạo lại, inject luôn
2. Kiểm tra bảng **"ViewModels đã có"** — nếu screen đã có ViewModel thì extend, không tạo mới
3. Kiểm tra **"Screens đã có"** — nếu route đã tồn tại, chỉ sửa screen đó
4. Copy đúng constructor pattern của `HeartRateRepositoryImpl(get(), get())` khi sửa Koin
5. Luôn map `HeartRateEntity → HeartRateRecord` qua `it.toDomainModel()` (extension đã có trong `data/mapper/HeartRateMapper.kt`)
