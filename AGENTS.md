# Agent Memory Protocol

Trước mỗi task, đọc:

- `.memory/context.md`
- `.memory/decisions.md`

Khi user chốt quyết định dài hạn bằng các cụm như `chốt là`, `quyết định dùng`,
`từ giờ`, hoặc `ghi nhớ`, cập nhật `.memory/decisions.md` hoặc
`.memory/context.md` ngay trong cùng task. Chỉ lưu điều user đã xác nhận; không
lưu secret, token hay dữ liệu cá nhân.

Sau khi thay đổi memory, báo rõ file đã cập nhật. Dùng `./.agents/scripts/memory.sh` để
ghi entry nếu phù hợp.

## KMP working agreement

Các quy tắc chuyên môn đầy đủ nằm tại [`docs/agent/KMP_AGENTS.md`](docs/agent/KMP_AGENTS.md). Tóm tắt bắt buộc:

- Giữ ổn định cross-platform Android/iOS và không import `java.*` hoặc `android.*` vào `commonMain`.
- Tuân thủ luồng `UI (Compose) → ViewModel → UseCase → Repository → DataSource`.
- Dùng Koin cho dependency injection, SQLDelight/Flow cho persistence và `expect`/`actual` hoặc interface injection cho API nền tảng.
- Không dùng `GlobalScope`/`runBlocking`; inject dispatcher và expose immutable `StateFlow`.
- Trước khi hoàn tất thay đổi quan trọng, kiểm tra Android (`./gradlew assembleDebug`) và iOS (`./gradlew iosSimulatorArm64Binaries`) khi phù hợp.

## Skill routing

Trước khi đọc skill chi tiết, xem [`.agents/skills/SKILLS_INDEX.md`](.agents/skills/SKILLS_INDEX.md)
và chỉ đọc skill phù hợp với task. Khi thêm hoặc đổi một skill trong `.agents/skills/`,
cập nhật `SKILLS_INDEX.md` trong cùng task.
