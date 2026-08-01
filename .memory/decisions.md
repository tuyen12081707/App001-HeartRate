# Decisions

Chưa có quyết định riêng nào được ghi nhận.

Format:

```md
## YYYY-MM-DD — Tên quyết định

- Quyết định: ...
- Lý do: ...
- Phạm vi: ...
- Trạng thái: active | superseded
```


## 2026-08-01 — Heart Rate MVP scope and architecture

- Quyết định: Chọn phương án 2 — refactor presentation/navigation có kiểm soát đồng thời redesign UI theo hướng Calm clinical. Luồng lõi là Disclaimer → Dashboard → nhập nhịp tim thủ công → Result → History; demo Android dọc 360–430dp, code iOS vẫn phải compile. Giữ bốn tab Dashboard/History/News/Profile; News và Profile giữ chức năng hiện tại. Core hoạt động offline; News lỗi mạng độc lập. Chỉ debug/demo seed dữ liệu nhịp tim mẫu 7 ngày; bản release không seed. History cho xem và xóa, chưa chỉnh sửa. Camera, huyết áp và blood sugar không nằm trong luồng demo chính.
- Lý do: Ưu tiên một MVP có thể demo ổn định nhưng vẫn giữ nền tảng KMP và khả năng mở rộng.
- Phạm vi: Presentation/navigation và các phần data/domain cần thiết cho luồng heart-rate MVP.
- Trạng thái: active

## 2026-08-01 — Shared UI state and verification

- Quyết định: Dùng `DataState.kt` hiện có làm state chung cho các feature với bốn trạng thái Idle, Loading, Error và Success; Composable chỉ render state, ViewModel điều phối intent/use case; lỗi giữ dữ liệu form khi có thể và có Retry. Kiểm thử domain validation/stats/7-day filtering/idempotent demo seed/delete; ViewModel state transitions và chống duplicate save; repository persistence insert-observe-delete; Android host test + assembleDebug; iOS shared compile; manual portrait demo checklist.
- Lý do: Tránh sealed state trùng lặp và tạo quy ước state nhất quán giữa các màn hình.
- Phạm vi: Presentation state và validation/verification của MVP.
- Trạng thái: active

## 2026-08-01 — Decision

- Nội dung: Task 2 clarification: demo seeding phải atomic — bảy heart-rate inserts và marker demo_seed_v1 nằm trong cùng transaction, retry không tạo duplicate. Dashboard day buckets dùng calendar day theo timezone thiết bị (injectable để test), không hard-code UTC.
- Trạng thái: active

## 2026-08-02 — Camera measurement presentation architecture

- Quyết định: Camera measurement phải tuân thủ pattern `BaseViewModel<UiState, Intent, SideEffect>`; `CameraMeasurementViewModel` nhận `CameraHeartRateSensor` qua constructor injection, còn `CameraMeasurementScreen` chỉ render state, gửi lifecycle intent và xử lý navigation side effect. Koin chỉ wire dependency và ViewModel, không inject sensor trực tiếp trong Composable.
- Lý do: Giữ nhất quán với kiến trúc KMP đã chốt, tách orchestration/lifecycle khỏi UI và cho phép test sensor bằng fake implementation.
- Phạm vi: `shared/commonMain` camera presentation và `presentationModule`.
- Trạng thái: active
