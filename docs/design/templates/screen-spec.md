# <Tên màn hình>

**Trạng thái:** Draft | Ready | Implemented

**Feature:** `<feature-name>`

**Code mapping:** `Screen.<NAME>` → `<Composable>` → `<ViewModel hoặc N/A>`

## Mục đích

<Người dùng cần hoàn thành gì ở đây?>

## Điểm vào và rời màn hình

| Sự kiện | Nguồn/đích | Dữ liệu truyền | Kết quả |
|---|---|---|---|
| Vào màn hình | `<Screen>` | `<data hoặc N/A>` | `<state mặc định>` |
| CTA chính | `<Screen>` | `<data hoặc N/A>` | `<điều hướng/thay đổi state>` |
| Back | `<Screen>` | N/A | `<điều hướng>` |

## Nội dung và tương tác

| Thành phần | Nội dung/biến thể | Hành động | Quy tắc |
|---|---|---|---|
| `<component>` | `<copy/state>` | `<event>` | `<validation/visibility>` |

## UI states

| State | Điều kiện | Hiển thị | Hành động tiếp theo |
|---|---|---|---|
| Loading | | | |
| Empty | | | |
| Error | | | |
| Success | | | |

## Accessibility và acceptance criteria

- <Mô tả content, thứ tự focus, cỡ chạm hoặc thông báo lỗi cần thiết.>
- [ ] Android và iOS hiển thị cùng hành vi.
- [ ] CTA/back đi đúng route trong flow.
- [ ] Có xử lý loading, empty, error và success khi màn hình có dữ liệu.
