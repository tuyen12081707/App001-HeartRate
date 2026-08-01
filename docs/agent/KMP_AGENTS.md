# KMP Working Agreement — App001HeartRate

Tài liệu này là mục lục quy tắc làm việc KMP của repository. Không lặp lại policy
ở nhiều nơi; hãy đọc đúng nguồn theo loại task.

- [SKILLS_INDEX.md](../../.agents/skills/SKILLS_INDEX.md): phụ lục định tuyến skill;
  đọc để chọn đúng skill trước khi mở tài liệu chi tiết.

## Nguồn bắt buộc

- [AGENTS.md](../../AGENTS.md): quy tắc agent áp dụng ở cấp repository.
- [kmp-development](../../.agents/skills/kmp-development/SKILL.md): quy trình KMP,
  Clean Architecture, source set, DI, Flow, validation và handoff.
- [project-context](../../.agents/skills/project-context/SKILL.md): class name,
  signature, data flow, navigation và context thực tế của codebase.
- [app001heartrate](../../.agents/skills/app001heartrate/SKILL.md): recipe và
  constraint kỹ thuật của App001HeartRate.
- [ui-components](../../.agents/skills/ui-components/SKILL.md): component và quy
  ước UI dùng chung.
- [navigation](../../.agents/skills/navigation/SKILL.md): state-based navigation.
- [GIT_COMMIT_RULES.md](../../.agents/workflows/GIT_COMMIT_RULES.md): quy tắc commit.
- [AUTO_WORKFLOW.md](../../.agents/workflows/AUTO_WORKFLOW.md): quy trình commit/push
  khi user yêu cầu Auto Workflow.

## Nguyên tắc tóm tắt

- Giữ khả năng compile Android và iOS; không import `android.*` hoặc `java.*` vào
  `commonMain`.
- Tuân thủ luồng `UI → ViewModel → UseCase → Repository → DataSource`.
- Dùng constructor injection/Koin cho dependency; ViewModel kế thừa
  `BaseViewModel<S, I, E>` và expose immutable state.
- Platform API dùng `expect/actual` hoặc interface injection; UI chỉ render state và
  phát intent.
- Sau thay đổi KMP, chạy validation phù hợp và cập nhật `project-context` nếu có
  class, signature, data flow hoặc navigation mới.
