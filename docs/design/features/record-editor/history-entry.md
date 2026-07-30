# History entry to edit record

**Trạng thái:** Planned

**Feature:** `record-editor`
**Code mapping:** `Screen.HISTORY` → `HistoryScreen` → `HistoryViewModel`

## Thay đổi hành vi

Chạm lên một `HeartRateItem` mở `RecordEditorMode.Edit(record.id)`. Thao tác xoá vẫn là hành động riêng và phải có xác nhận trước khi xoá.

## Acceptance criteria

- [ ] Item có affordance rõ ràng để chỉnh sửa (toàn hàng hoặc CTA Edit).
- [ ] Không kích hoạt edit khi người dùng chạm CTA xoá.
- [ ] Sau update thành công, History nhận dữ liệu mới từ Flow database mà không cần reload thủ công.
