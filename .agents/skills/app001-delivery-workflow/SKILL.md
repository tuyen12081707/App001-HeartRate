---
name: app001-delivery-workflow
description: Use when implementing, fixing, reviewing, committing, or shipping any change in the App001HeartRate repository and a repeatable delivery workflow is needed.
---

# App001HeartRate Delivery Workflow

Đây là workflow tương thích Codex cho App001HeartRate, lấy cảm hứng từ các
workflow plugin Claude nhưng không phụ thuộc `claude`, slash command, hook riêng
hay Claude API.

## 1. Preflight

Trước khi sửa:

1. Đọc `.memory/context.md` và `.memory/decisions.md`.
2. Đọc `AGENTS.md` và `.agents/skills/SKILLS_INDEX.md`.
3. Chọn rồi chỉ đọc skill phù hợp với task; không đọc toàn bộ skill catalog.
   `BRAINSTORM.md` và `project-context` chỉ đọc khi index hoặc task yêu cầu.
4. Kiểm tra `git status` và xác định thay đổi có sẵn; không ghi đè hoặc gom chúng
   vào commit nếu không thuộc task hiện tại.
5. Xác định module/source set bị ảnh hưởng và viết plan ngắn bằng task list.

## 2. Plan trước implementation

- Feature/behavior mới: dùng TDD — test hoặc reproduction trước, sau đó code tối
  thiểu, rồi refactor.
- Bug/build failure: xác định root cause và tái hiện trước khi sửa.
- UI/navigation/data/DI KMP: kiểm tra code hiện tại và signature thực tế trước khi
  tạo class hoặc route mới.
- Nếu user chốt quyết định dài hạn, cập nhật memory trong cùng task và báo file.

## 3. Implement theo checkpoint

Giữ các checkpoint nhỏ, có thể review độc lập:

1. Domain contract/model/use case.
2. Data/repository/database hoặc platform adapter.
3. ViewModel state/intent/side effect.
4. Screen/navigation/DI.
5. Test và project context.

Không gọi API/database trực tiếp từ Composable; không đưa `android.*` hoặc `java.*`
vào `commonMain`; ViewModel dùng `BaseViewModel<S, I, E>` và constructor injection.

## 4. Review trước khi hoàn tất

Đọc lại diff như reviewer độc lập:

- Có file thừa, generated file, secret, log debug hoặc hard-coded business rule không?
- Có vi phạm commonMain/iOS, lifecycle/cancellation, DI hoặc navigation không?
- Test có kiểm tra error, retry, cancellation, duplicate action và persistence khi
  phù hợp không?
- `project-context`/`BRAINSTORM.md` có cần cập nhật không?

## 5. Verification matrix

Chạy mức kiểm tra nhỏ nhất đủ chứng minh thay đổi, ghi rõ lệnh và kết quả:

| Phạm vi | Kiểm tra tối thiểu |
|---|---|
| common/domain/ViewModel | `./gradlew :shared:testAndroidHostTest` |
| common/Android compile | `./gradlew :shared:compileKotlinAndroid` |
| iOS/shared API | `./gradlew :shared:compileKotlinIosSimulatorArm64` khi môi trường hỗ trợ |
| Android app/UI/resources | `./gradlew :androidApp:assembleDebug` |
| database/DI/navigation | test liên quan + compile/build của module bị ảnh hưởng |

Không tuyên bố “đã xong/pass” nếu lệnh bắt buộc còn fail; ghi rõ blocker và bước
tiếp theo.

## 6. Commit và handoff

- Chỉ commit khi user yêu cầu hoặc workflow task đã nêu rõ commit.
- Stage bằng danh sách file có chủ đích; không dùng `git add .` nếu working tree có
  thay đổi ngoài scope.
- Tuân thủ `.agents/workflows/GIT_COMMIT_RULES.md`. Không push nếu user chưa yêu cầu.
- Handoff **bằng tiếng Anh**: summarize the main changes, affected files,
  verification commands and results, remaining risks, next step, and commit hash
  when available.

## Mapping từ Claude sang Codex

- `CLAUDE.md`/plugin instructions → `AGENTS.md` và `.agents/skills/*`.
- Slash command → trigger của skill hoặc yêu cầu tự nhiên của user.
- `claude -p`/headless agent → Codex conversation + `functions.exec`/skills.
- Claude hook → script/workflow repo-local; chỉ chạy khi user yêu cầu hoặc CI gọi.
- Subagent/multi-agent → chỉ dùng khi task độc lập và được phép; không tự spawn cho
  thay đổi tuần tự hoặc nhỏ.
