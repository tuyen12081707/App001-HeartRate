# Project Memory

Thư mục này là bộ nhớ bền vững của project, được commit cùng source code.

## Quy ước

- `decisions.md`: các quyết định đã chốt và còn hiệu lực.
- `context.md`: bối cảnh, ưu tiên và constraint dài hạn.
- `inbox.md`: ghi chú chưa được xác nhận; không coi là quyết định.

Agent phải đọc `context.md` và `decisions.md` trước khi bắt đầu task.

Khi user nói một trong các câu như `chốt là`, `quyết định dùng`, `từ giờ`, hoặc
`ghi nhớ`, agent ghi lại nội dung vào memory tương ứng. Không tự lưu dữ liệu
nhạy cảm, thông tin cá nhân, secret hoặc token.

## Lệnh nhanh

```bash
./.agents/scripts/memory.sh show
./.agents/scripts/memory.sh search "từ khóa"
./.agents/scripts/memory.sh decision "Dùng state-based navigation cho app"
./.agents/scripts/memory.sh context "Ưu tiên ổn định Android và iOS"
./.agents/scripts/memory.sh inbox "Cần xác nhận cách xử lý onboarding"
```
