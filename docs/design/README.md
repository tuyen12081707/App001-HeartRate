# Design source of truth

Thư mục này là nơi định nghĩa luồng và đặc tả UI bằng văn bản. Có thể đọc, review và diff hoàn toàn từ terminal. Mermaid được GitHub, IntelliJ và nhiều Markdown preview render trực tiếp; không cần lưu file thiết kế nhị phân.

## Cấu trúc

```text
docs/design/
├── flows/                 # Luồng dùng chung giữa các màn hình (.mmd)
├── features/<tên-feature>/ # Đặc tả từng màn hình (.md)
└── templates/              # Mẫu tạo đặc tả
```

## Cách dùng

1. Sửa hoặc thêm flow tại `flows/` trước.
2. Copy `templates/screen-spec.md` vào `features/<feature>/` cho mỗi màn hình thay đổi.
3. Điền mapping đến `Screen`/Composable/ViewModel thật trong code.
4. Khi code thay đổi navigation hoặc state UI, cập nhật flow/spec trong cùng commit.

Xem flow hiện tại: [app-navigation.mmd](flows/app-navigation.mmd). Khi nhờ Codex xây UI từ flow, dùng `Use $flow-to-design ...` và nêu tên feature.
