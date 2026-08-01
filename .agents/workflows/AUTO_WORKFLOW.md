# 🤖 Tự Động Hóa Workflow (Auto Workflow)

File này định nghĩa quy trình chuẩn mà AI Agent phải TUYỆT ĐỐI tuân thủ khi User yêu cầu: **"Hoàn thành Task và chạy Auto Workflow"**.

Workflow chi tiết nằm trong skill [`app001-delivery-workflow`](../skills/app001-delivery-workflow/SKILL.md). File này là entrypoint cho commit/push automation; không phụ thuộc Claude Code.

Agent không được hỏi lại, mà phải chạy ngầm các lệnh terminal (sử dụng Run Command) theo đúng thứ tự sau:

### 1. Kiểm tra Code
* Tự động kiểm tra lại toàn bộ file vừa sửa, đảm bảo không có lỗi C-Interop (nếu có iOS) và không có Memory Leak.

### 2. Tự Động Commit theo Rule
* Đọc kỹ file `GIT_COMMIT_RULES.md` để lấy định dạng.
* Stage các file thuộc task bằng danh sách có chủ đích; không gom thay đổi ngoài scope.
* Chạy lệnh `git commit -m "[TICKET-ID] <type>(<scope>): <subject>"` và đảm bảo commit chia nhỏ hợp lý.

### 3. Tự Động Push Code
* Chạy lệnh `git push`.
* Nếu lỗi chưa set upstream, tự động chạy `git push --set-upstream origin <tên-nhánh-hiện-tại>`.

### 4. Báo Cáo và Trả Link Pull Request
* Xuất ra cho User nội dung PR chuẩn (PR Description) bám sát các thay đổi vừa làm (bao gồm Summary, Key Features, Checklists).
* Trả về cho User một đường link có định dạng: `https://github.com/<user>/<repo>/pull/new/<tên-nhánh>` để User bấm vào tạo PR trong 1 click.

---

**💡 Hướng dẫn dành cho User (Bạn chỉ cần copy/paste câu này):**
> *"Tôi đã code xong Task [Tên Task]. Hãy dùng `$app001-delivery-workflow` và chạy Auto Workflow dựa trên file `.agents/workflows/AUTO_WORKFLOW.md` để kiểm tra, commit chuẩn GitLab, push lên và tạo sẵn template PR cho tôi!"*
