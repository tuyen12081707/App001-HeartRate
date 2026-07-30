# Record editor — tạo và chỉnh sửa nhịp tim

**Trạng thái:** Ready

**Feature:** `record-editor`

**Code mapping:** `Screen.ADD_RECORD` → `AddRecordScreen` → `AddRecordViewModel`
**Đề xuất navigation:** dùng cùng `Screen.ADD_RECORD`, truyền `RecordEditorMode` thay vì thêm screen riêng.

## Mục đích

Cho người dùng tạo bản ghi nhịp tim mới hoặc sửa một bản ghi đã có. Cùng một layout và validation được dùng cho hai mode để tránh hai luồng UI bị lệch nhau.

## Mode và dữ liệu

| Mode | Điểm vào | Dữ liệu ban đầu | Thao tác lưu |
|---|---|---|---|
| `Create` | Dashboard/News hoặc sau camera | Giá trị BPM từ camera nếu có; các trường khác mặc định | Tạo record mới trong database |
| `Edit(recordId)` | Chạm một record ở History | Tải record theo `recordId` và prefill toàn bộ trường | Cập nhật đúng record trong database |

`recordId` là nguồn định danh duy nhất cho edit; không truyền nguyên object giữa screen. Khi load/saving thất bại, ở lại editor và hiển thị lỗi có thể hành động.

## Điểm vào và rời màn hình

| Sự kiện | Nguồn/đích | Dữ liệu truyền | Kết quả |
|---|---|---|---|
| Tạo mới | Dashboard/News → `ADD_RECORD` | `RecordEditorMode.Create`, `prefilledBpm?` | Form trống hoặc BPM prefilled |
| Sửa | History → `ADD_RECORD` | `RecordEditorMode.Edit(recordId)` | Form được tải từ database |
| Lưu create | Editor → Result | `recordId`, `bpm`, `bodyState`, `RecordSaveMode.Created` | Record đã được insert |
| Lưu edit | Editor → Result | `recordId`, `bpm`, `bodyState`, `RecordSaveMode.Updated` | Record đã được update |
| Back | Create → Dashboard; Edit → History | N/A | Không lưu thay đổi chưa xác nhận |

## UI states

| State | Điều kiện | Hiển thị | Hành động tiếp theo |
|---|---|---|---|
| Loading | Đang tải record khi edit hoặc đang lưu | Loader, không cho gửi lặp | Chờ kết quả |
| Error | Không tải/lưu được | Lỗi rõ ràng + Retry | Retry hoặc Back |
| Success | Form hợp lệ | CTA `Lưu` | Insert/update rồi đi Result |

## Acceptance criteria

- [ ] Chạm item ở History mở editor với các trường đúng bản ghi.
- [ ] Create thực hiện insert; Edit chỉ update record có `recordId` tương ứng.
- [ ] Không thể gửi hai lần khi đang lưu.
- [ ] Sau khi lưu thành công, Result hiển thị trạng thái Created/Updated chính xác trên Android và iOS.
