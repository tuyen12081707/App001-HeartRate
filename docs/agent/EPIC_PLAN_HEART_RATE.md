# Epic: Heart Rate Tracker Application

## 1. Product Vision
Xây dựng một ứng dụng theo dõi nhịp tim (Heart Rate) với giao diện Compose UI mượt mà. Ứng dụng cho phép người dùng nhập nhịp tim thủ công (Manual Entry), xem lại lịch sử đo (History List), theo dõi biểu đồ thống kê, và trong tương lai sẽ hỗ trợ đo nhịp tim bằng Camera/Flash.

## 2. Core Entities (Domain Model)
* **`HeartRateRecord`**
    * `id`: String (UUID) hoặc Int (Auto-increment)
    * `bpm`: Int (Beats per minute)
    * `timestamp`: Long (Epoch time)
    * `measureType`: Enum (MANUAL, CAMERA_SENSOR)
    * `bodyState`: Enum (RESTING, EXERCISING, SLEEPING, etc.)
    * `note`: String (Tùy chọn)

## 3. Phân rã tiến trình (Implementation Phases)
Agent cần thực hiện tuần tự các Phase dưới đây. Tuyệt đối tuân thủ Clean Architecture và các quy tắc trong `KMP_AGENTS.md`.

### Phase 1: Foundation & Data Layer (Core Database)
* **Mục tiêu:** Khởi tạo cấu trúc Database và các Repository cơ bản.
* **Chi tiết công việc:**
    * Setup thư viện Database (SQLDelight hoặc Room).
    * Tạo bảng `HeartRate` với các trường tương ứng Core Entity.
    * Viết các Query cơ bản: `insert`, `getAllRecords` (sắp xếp mới nhất lên đầu), `deleteRecord`, `getAverageBpm`.
    * Tạo `HeartRateRepository` interface ở Domain layer và `HeartRateRepositoryImpl` ở Data layer.
    * Config Koin module (`dataModule`, `domainModule`).

### Phase 2: Feature - Add Manual Record (Đo bằng tay)
* **Mục tiêu:** Xây dựng màn hình/bottom sheet cho phép người dùng nhập BPM bằng bàn phím.
* **Chi tiết công việc:**
    * Tạo `AddRecordUseCase`.
    * Tạo `AddRecordViewModel` (Xử lý validate: BPM phải từ 30 đến 250).
    * Giao diện: Form nhập số BPM, chọn ngày giờ (mặc định là hiện tại), chọn trạng thái cơ thể (Dropdown/Chips), và nút "Save".
    * Xử lý side-effect: Hiện Snackbar báo lưu thành công và quay về màn trước.

### Phase 3: Feature - History List (Danh sách lịch sử)
* **Mục tiêu:** Hiển thị danh sách các lần đo nhịp tim.
* **Chi tiết công việc:**
    * Tạo `GetHeartRateHistoryUseCase` trả về `Flow<List<HeartRateRecord>>`.
    * Xây dựng `HistoryViewModel` hứng dữ liệu từ Flow và đẩy ra `UiState`.
    * Giao diện: Sử dụng `LazyColumn`. Mỗi item hiển thị BPM lớn, icon trạng thái, thời gian đo, và tag (Manual/Camera).
    * Tính năng UI: Hỗ trợ "Swipe to delete" để xóa một record.

### Phase 4: Feature - Dashboard & Statistics (Thống kê)
* **Mục tiêu:** Trực quan hóa dữ liệu đo được.
* **Chi tiết công việc:**
    * Lấy dữ liệu 7 ngày gần nhất.
    * Tính toán: Nhịp tim trung bình, cao nhất, thấp nhất.
    * Giao diện: Vẽ biểu đồ đường (Line Chart) đơn giản bằng Jetpack Compose Canvas hoặc dùng thư viện.
    * Hiển thị card tóm tắt tình trạng sức khỏe dựa trên BPM.

### Phase 5: Feature - Camera Measurement (Đo bằng Camera - Future Scope)
* **Mục tiêu:** Sử dụng Camera và Flashlight để phát hiện thay đổi màu sắc ở ngón tay, từ đó tính BPM.
* **Chi tiết công việc:**
    * Thiết lập permission Camera.
    * Viết module xử lý Image Processing / CameraX (Phần này bắt buộc dùng Native implementation qua `expect`/`actual` nếu làm KMP).
    * Hiển thị UI đếm ngược và animation nhịp đập khi đang phân tích.