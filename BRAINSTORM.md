# App001 Heart Rate — Brainstorm & Persistent Memory

File này là nơi lưu ý tưởng, quyết định sản phẩm và trạng thái dài hạn của project.
Mọi task KMP phải đọc file này cùng `.agents/skills/project-context/SKILL.md` trước
khi lập plan.

## Quy ước sử dụng

- `IDEA`: ý tưởng chưa được duyệt, không tự động triển khai.
- `DECIDED`: user đã chốt; task sau phải tôn trọng cho đến khi có quyết định mới.
- `IN_PROGRESS`: đang triển khai trong workspace.
- `DONE`: đã triển khai và validation thành công.
- Mỗi mục cần ghi ngày, phạm vi, lý do và tác động kỹ thuật.
- Khi một quyết định thay đổi kiến trúc/code map, cập nhật thêm
  `.agents/skills/project-context/SKILL.md`.

## Decisions

### DECIDED — Kiến trúc KMP

- Ngày: 2026-07-30
- Giữ `com.android.kotlin.multiplatform.library`; không đổi `shared` sang
  `com.android.library` chỉ để né lỗi resource.
- Dùng Clean Architecture, SOLID, composition over inheritance và state-based
  navigation hiện có.
- Ngưỡng nghiệp vụ nằm trong domain; UI không hard-code business rules.

### DECIDED — Compose Resources trên Android/AGP 9

- Ngày: 2026-07-30
- Compose Resources 1.6.11 đọc strings bằng ClassLoader, vì vậy generated resources
  phải nằm ở root APK `composeResources/...`, không nằm dưới `assets/`.
- `androidApp/build.gradle.kts` đăng ký generated Java resources bằng Variant API và
  task tương thích configuration cache.

### DONE — Blood Pressure New Record

- Ngày: 2026-07-30
- UI theo Figma SP005 node `33:2705`.
- Date card mở dialog wheel gồm giờ, phút, ngày, tháng, năm; Cancel/OK theo mockup.
- Systolic/Diastolic cập nhật live `BloodPressureLevel` từ domain policy.
- Pulse được lưu và validation bằng domain input constraints; pulse không quyết định
  Hypotension/Hypertension vì các mức này dựa trên huyết áp.
- Timestamp được truyền xuyên ViewModel → use case → repository, không thay lại bằng
  thời gian hiện tại khi Save.
- Validation Android/iOS đã pass. Commit được thực hiện sau khi cập nhật memory.

### DONE — Deterministic Dashboard Data and Demo Seed

- Ngày: 2026-08-01
- Dùng `Clock` inject được cho timestamp, dashboard và seed; `SystemClock` bọc
  `getCurrentTimeMillis()` hiện có nên vẫn compile chung Android/iOS.
- Dashboard chỉ aggregate bảy epoch-day bucket gần nhất theo clock, loại dữ liệu cũ
  và tương lai, chart point sắp xếp từ cũ đến mới.
- Demo seed gồm đúng bảy bản ghi manual cố định và marker `demo_seed_v1` chỉ được ghi
  sau khi tất cả insert thành công; lần chạy sau không insert thêm.
- Validation common Android host test, Android main compile và iOS simulator ARM64
  compile đã pass.

## Brainstorm Inbox

### IDEA — Blood Pressure History

- Hiển thị lịch sử huyết áp riêng với SYS/DIA/Pulse, level màu và filter theo thời gian.
- Chưa được duyệt triển khai.

### IDEA — Medical threshold source/version

- Gắn nguồn và version cho bộ ngưỡng phân loại; cho phép thay policy mà không sửa UI.
- Chưa được duyệt triển khai.

### IDEA — Date/time picker reuse

- Nếu màn Heart Rate/Blood Sugar cũng cần chọn thời gian, extract dialog thành
  stateless component dùng chung với state hoisting.
- Chỉ extract khi có ít nhất hai consumer.

## Validation Log

- 2026-07-30: `:shared:compileAndroidMain` pass sau khi thêm date dialog và live
  classification.
- 2026-07-30: `:shared:testAndroidHostTest`,
  `:shared:compileKotlinIosSimulatorArm64` và `:androidApp:assembleDebug` pass.
