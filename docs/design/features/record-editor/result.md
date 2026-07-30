# Record result — trạng thái sau khi lưu

**Trạng thái:** Ready

**Feature:** `record-editor`

**Code mapping:** `Screen.RESULT` → `ResultScreen` → `N/A hiện tại`

## Mục đích

Xác nhận thao tác database đã thành công và cho biết đó là bản ghi mới hay bản ghi đã chỉnh sửa.

## Input bắt buộc

| Dữ liệu | Mục đích |
|---|---|
| `recordId: Long` | Định danh record vừa lưu |
| `bpm: Int` | Hiển thị giá trị đã lưu |
| `bodyState: String` | Hiển thị trạng thái cơ thể |
| `saveMode: Created | Updated` | Quyết định copy và analytics/event sau này |

## Nội dung và tương tác

| Thành phần | Create | Edit | Hành động |
|---|---|---|---|
| Tiêu đề/trạng thái | `Đã lưu bản ghi mới` | `Đã cập nhật bản ghi` | Phản ánh `saveMode` |
| BPM + body state | Giá trị vừa lưu | Giá trị vừa cập nhật | Chỉ đọc |
| CTA chính | Về Dashboard | Về History | Điều hướng theo mode |
| CTA phụ | Đo lại | Sửa tiếp (tuỳ chọn) | Create → editor Create; Edit → editor Edit(recordId) |

## Acceptance criteria

- [ ] Result chỉ xuất hiện sau khi database trả về thành công.
- [ ] Copy và CTA phù hợp `saveMode`; không suy luận mode từ màn hình trước đó.
- [ ] Back/CTA không làm mất hoặc tạo lại dữ liệu đã lưu.
