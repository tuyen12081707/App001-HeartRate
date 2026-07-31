# Agent Memory Protocol

Trước mỗi task, đọc:

- `.memory/context.md`
- `.memory/decisions.md`

Khi user chốt quyết định dài hạn bằng các cụm như `chốt là`, `quyết định dùng`,
`từ giờ`, hoặc `ghi nhớ`, cập nhật `.memory/decisions.md` hoặc
`.memory/context.md` ngay trong cùng task. Chỉ lưu điều user đã xác nhận; không
lưu secret, token hay dữ liệu cá nhân.

Sau khi thay đổi memory, báo rõ file đã cập nhật. Dùng `./agent/memory.sh` để
ghi entry nếu phù hợp.

