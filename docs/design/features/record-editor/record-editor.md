# Record editor — tạo nhịp tim

**Trạng thái:** MVP ready (create-only)

**Feature:** `record-editor`

**Code mapping:** `AppRoute.AddHeartRate` → `AddRecordScreen` → `AddRecordViewModel`
**Navigation:** `AppNavigator` điều hướng tới `AppRoute.Result(recordId)` sau khi lưu thành công. `recordId` chỉ được truyền qua route; Result dùng key `result-{recordId}` để lấy đúng `ResultViewModel`.

## Mục đích

Cho người dùng tạo bản ghi nhịp tim mới bằng thao tác nhập thủ công. MVP chưa hỗ trợ chỉnh sửa bản ghi đã lưu; History chỉ cho xem và xóa sau khi xác nhận. Khi cần bổ sung edit, giữ nguyên route typed và thêm một route/mode riêng sau khi có quyết định sản phẩm.

## Mode và dữ liệu

| Mode | Điểm vào | Dữ liệu ban đầu | Thao tác lưu |
|---|---|---|---|
| `Create` (MVP) | Dashboard → `AppRoute.AddHeartRate` | BPM mặc định trong khoảng 30–250; body state và note do người dùng nhập | Tạo record mới trong database, rồi điều hướng `AppRoute.Result(recordId)` |
| `Edit(recordId)` (ngoài MVP) | Chưa có điểm vào trong `MainTab`/History | Sẽ tải record theo `recordId` khi tính năng được duyệt | Sẽ cập nhật đúng record trong database |

Không truyền nguyên object giữa screen. Khi load/saving thất bại, ở lại editor và hiển thị lỗi có thể hành động.

## Điểm vào và rời màn hình

| Sự kiện | Nguồn/đích | Dữ liệu truyền | Kết quả |
|---|---|---|---|
| Tạo mới | Dashboard → `AppRoute.AddHeartRate` | Không cần payload route | Mở form create-only |
| Lưu | Editor → `AppRoute.Result(recordId)` | `recordId: Long` | Record đã được insert và Result đọc lại từ persistence |
| Thêm bản ghi khác | Result → `AppRoute.AddHeartRate` | Không truyền record cũ | Form được reset cho lần nhập mới |
| Back | Editor → navigator back stack | N/A | Không lưu thay đổi chưa xác nhận |
| Xóa | History → dialog xác nhận → `HistoryViewModel.DeleteRecord(recordId)` | `recordId: Long` | Xóa persistence rồi reload History |

## UI states

| State | Điều kiện | Hiển thị | Hành động tiếp theo |
|---|---|---|---|
| Loading | Đang tải record khi edit hoặc đang lưu | Loader, không cho gửi lặp | Chờ kết quả |
| Error | Không tải/lưu được | Lỗi rõ ràng + Retry | Retry hoặc Back |
| Success | Form hợp lệ | CTA `Lưu` | Insert/update rồi đi Result |

## Acceptance criteria

- [x] Create thực hiện insert và chuyển tới Result bằng `recordId`.
- [ ] Edit record ở History (ngoài MVP, chưa triển khai).
- [ ] Không thể gửi hai lần khi đang lưu.
- [ ] Sau khi lưu thành công, Result đọc đúng record trên Android và iOS.
