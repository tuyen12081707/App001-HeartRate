---
name: kmp-development
description: "Chuẩn hóa việc lập plan, triển khai và review tính năng Kotlin Multiplatform (KMP) cho App001HeartRate. Kích hoạt khi task chạm vào commonMain, androidMain, iosMain, Compose Multiplatform, expect/actual, Koin, SQLDelight, Ktor, navigation, UI hoặc test."
---

# KMP Development — App001HeartRate

Đây là **nguồn policy duy nhất** cho mọi thay đổi KMP trong project. Với từng task,
đọc phần liên quan của `../project-context/SKILL.md` (bản đồ code) hoặc
`../app001heartrate/SKILL.md` (recipe/constraint kỹ thuật), thay vì gom các policy
vào nhiều skill. Nếu có mâu thuẫn, ưu tiên code thực tế trong repo rồi cập nhật lại
context.

## Quy trình trước khi code

1. Xác định phạm vi: feature/UI/API/database/platform; module và source set bị ảnh hưởng.
2. Đọc file hiện tại liên quan, không đoán tên class hoặc signature.
3. Tạo và duy trì task list/plan có trạng thái `pending`, `in_progress` hoặc
   `completed`. Khởi tạo khi bắt đầu task và cập nhật sau mỗi phase có ý nghĩa; chỉ
   một task được ở trạng thái `in_progress`.
4. Tóm tắt ngắn plan: mục tiêu, file dự kiến, data flow, rủi ro và validation.
5. Giữ thay đổi nhỏ, tương thích kiến trúc hiện tại; không thêm dependency lớn nếu chưa có lý do.
6. Đọc `../../../BRAINSTORM.md` để lấy quyết định dài hạn và ý tưởng liên quan. Mục
   `IDEA` chỉ là đề xuất, không được tự động đưa vào scope nếu user chưa duyệt.

## Quy tắc kiến trúc

- Code dùng chung đặt trong `shared/src/commonMain`; Android/iOS-specific đặt đúng
  `androidMain`/`iosMain`.
- Dùng `expect/actual` cho camera, driver, dispatchers, time và API platform; không
  import Android/iOS API vào `commonMain`.
- Giữ luồng `data (DTO/client/mapper/repository impl) → domain
  (model/repository/use case) → presentation (UiState/Intent/SideEffect/ViewModel/Screen)`.
- ViewModel phải kế thừa `BaseViewModel<S, I, E>`, expose immutable state và xử lý
  coroutine qua `viewModelScope`/`AppDispatchers`.
- Mọi dependency injection phải cập nhật đúng module trong
  `shared/.../di/Koin.kt`; platform binding cập nhật trong `PlatformModule.android.kt`
  và `.ios.kt` khi cần.
- Navigation là state-based trong `App.kt`: thêm `Screen`, state truyền dữ liệu,
  nhánh render và bottom-bar visibility; không đưa Jetpack Navigation vào.
- Database dùng SQLDelight schema trong `shared/src/commonMain/sqldelight`; không sửa
  generated code trực tiếp. Sau khi đổi schema/query phải chạy generate/build.
- UI dùng Compose Multiplatform, `AppTheme`, MaterialTheme color scheme và component
  dùng chung trước khi tạo component mới.

## SOLID và Clean Architecture

- Dependency direction đi vào trong: `presentation → domain ← data`; domain không
  phụ thuộc DTO, SQLDelight, Ktor, Android hoặc iOS.
- **SRP:** mỗi class/use case có một lý do thay đổi. Repository điều phối nguồn dữ
  liệu; mapper chỉ map; ViewModel chỉ điều phối state/intent/side effect; composable
  chỉ render và phát event.
- **OCP:** mở rộng bằng interface, sealed type, mapper hoặc composition; không sửa
  logic ổn định bằng chuỗi `if/else` theo từng feature nếu có thể tách policy.
- **LSP:** implementation của repository/sensor phải giữ đúng contract, semantics
  lỗi và lifecycle của interface.
- **ISP:** interface nhỏ theo capability (`ReadHistory`, `SaveRecord`, ...); tránh
  interface “god object” buộc caller phụ thuộc hàm không dùng.
- **DIP:** domain nhận abstraction qua constructor; Koin là nơi wire implementation.
  Không gọi service locator hoặc khởi tạo client/database trực tiếp trong ViewModel.
- Không tạo layer/use case chỉ để bọc một dòng vô nghĩa; chỉ thêm abstraction khi có
  business rule, boundary platform, khả năng thay thế hoặc nhu cầu test rõ ràng.
- DTO/entity không được rò rỉ vào UI; lỗi từ data được map thành domain/presentation
  error có thể hiển thị hoặc xử lý được.

## Composition over inheritance

- Ưu tiên constructor injection, interface nhỏ, wrapper/decorator, delegation,
  extension function và composable có slot API thay vì cây kế thừa sâu.
- Chỉ kế thừa khi quan hệ “is-a” ổn định và contract có thể thay thế an toàn. `BaseViewModel`
  là ngoại lệ chuẩn của project; không tạo thêm `BaseXxxViewModel` chỉ để gom vài hàm.
- Không dùng abstract base screen/repository với nhiều cờ trạng thái. Tách collaborator
  theo trách nhiệm và compose chúng trong feature.
- Component UI tái sử dụng bằng composable/stateless + state hoisting; không giữ state
  nghiệp vụ trong component dùng chung.
- Khi có hành vi biến đổi, truyền strategy/policy qua constructor hoặc parameter thay
  vì override method ở subclass.

## Kotlin Flow và `combine`

- Dùng `combine` khi cần tạo một state từ nhiều stream độc lập; luôn map về một
  immutable `UiState` duy nhất và cân nhắc `distinctUntilChanged()`.
- `combine` phát lại khi bất kỳ upstream thay đổi; dùng `zip` chỉ khi cần ghép đúng
  từng cặp emission. Dùng `flatMapLatest` khi stream mới phụ thuộc vào key/query.
- Ưu tiên `StateFlow` có initial value cho state UI. Không lồng nhiều `collect` tuần tự
  để ghép dữ liệu; tránh `GlobalScope`, tự launch trong composable hoặc tạo collector
  mới mỗi lần recomposition.
- Collection chạy trong `viewModelScope` với dispatcher được inject; huỷ đúng theo
  lifecycle. `catch`, loading và error phải được biểu diễn rõ trong state/side effect.
- Ví dụ pattern:

  ```kotlin
  val uiState: StateFlow<DashboardUiState> =
      combine(historyFlow, settingsFlow) { history, settings ->
          DashboardUiState(history = history, settings = settings)
      }
      .distinctUntilChanged()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
  ```

  Không dùng `stateIn` nhiều lần cho cùng một pipeline; giữ một nguồn state chuẩn để
  screen collect.

## Recipe theo loại task

- API: DTO/serialization → client → domain model → repository interface/impl →
  use case → Koin → ViewModel/Screen → test.
- Database: `.sq` schema/query → mapper → repository → use case → Koin → UI → test.
- Màn hình: `UiState`/`Intent`/`SideEffect` → ViewModel → composable → App navigation
  → reusable components → preview/test nếu phù hợp.
- Platform: khai báo `expect` trong common, `actual` cho cả Android và iOS, rồi wire
  vào `platformModule`.

## Validation bắt buộc

- Chạy formatter/linter nếu project có cấu hình.
- Tối thiểu chạy `./gradlew :shared:compileKotlinAndroid` hoặc task tương đương sau
  thay đổi common/Android; đổi iOS thì kiểm tra compile/link iOS nếu môi trường hỗ trợ.
- Đổi database/DI/navigation phải chạy build liên quan và kiểm tra generated code.
- Với logic domain/ViewModel, thêm hoặc cập nhật `commonTest`; không bỏ qua test chỉ
  vì thay đổi bắt đầu từ UI.
- Test repository bằng fake implementation, test use case bằng contract, test Flow
  với nhiều emission và cancellation; không phụ thuộc database/network thật trong
  unit test.
- Báo rõ lệnh đã chạy, kết quả và phần chưa thể verify.

## Persistent project context

Sau khi thêm class/file/flow mới, cập nhật `../project-context/SKILL.md` ở đúng mục
liên quan. Ghi tên file, public signature, data flow và navigation/DI wiring; chỉ ghi
thông tin đã kiểm chứng từ code.

Sau khi user chốt một hướng sản phẩm/kỹ thuật hoặc task tạo ra ý tưởng cần giữ lại,
cập nhật `../../../BRAINSTORM.md` với trạng thái `IDEA`, `DECIDED`, `IN_PROGRESS`
hoặc `DONE`. Không ghi suy đoán như một quyết định đã duyệt.

Rule mới chỉ được thêm vào skill này sau khi user chấp thuận rõ ràng.

## Handoff format

Kết thúc mỗi task bằng: thay đổi chính, file ảnh hưởng, validation đã chạy, rủi ro còn lại
và đề xuất bước tiếp theo. Nếu task chỉ review/diagnose thì không tự ý sửa code.

## References

- Chi tiết dependency/source set hiện tại: `../project-context/SKILL.md`
- SOP API/UI của project: `../app001heartrate/SKILL.md`
