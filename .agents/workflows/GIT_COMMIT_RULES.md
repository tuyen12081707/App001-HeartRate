# GitLab Commit Rules

Luôn tuân thủ định dạng commit sau trước khi push code:

**Format:** `[TICKET-ID] <type>(<scope>): <subject>`

### 1. Types
*   **`feat`**: Tính năng mới
*   **`fix`**: Sửa lỗi
*   **`hotfix`**: Sửa lỗi khẩn cấp (Production)
*   **`ui`**: Thay đổi giao diện, không đổi logic
*   **`refactor`**: Tái cấu trúc code
*   **`docs`**: Thay đổi tài liệu
*   **`test`**: Thêm/sửa test
*   **`chore`**: Công việc vặt, cấu hình
*   **`perf`**: Cải thiện performance

### 2. Scope (Tùy chọn)
*   Mô tả phần của codebase bị ảnh hưởng (VD: `auth`, `login`, `repository`, `compose`...).

### 3. Subject
*   Viết ở thì hiện tại, imperative mood (VD: `add`, `fix` thay vì `added`, `fixed`).
*   Không viết hoa chữ cái đầu.
*   Không kết thúc bằng dấu chấm.
*   Tối đa 50 ký tự.

### 4. Body & Footer (Tùy chọn)
*   Cách 1 dòng trống so với subject. Mô tả lý do (Tại sao lỗi) và cách thực hiện.

### 5. Checklist
*   Self-Review: Đã tự đọc lại Git Diff.
*   Clean Code: Đã xóa sạch code nháp, Log.d.
*   Build Check: Code mới có build thành công trên máy local.
*   Phân chia: Tách nhỏ commit nếu làm nhiều tính năng.
