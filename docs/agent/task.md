# Epic 2: Lịch sử & Đồ thị Nhịp tim (Persistence & Trends)

Hiện tại các màn hình UI cơ bản (Epic 1) đã hoàn tất. Mục tiêu của Epic 2 là giúp ứng dụng thực sự "sống" bằng cách lưu trữ dữ liệu và vẽ biểu đồ theo dõi sức khoẻ người dùng.

## 1. Lưu trữ dữ liệu cục bộ (Local Database)
* **Công nghệ:** Sử dụng Room (hoặc SQLDelight) hỗ trợ KMP để thiết lập Database.
* **Mô hình Dữ liệu:** Bảng `HeartRateRecord` (id, bpm, timestamp, status, bodyState).
* **Tính năng:** Khi người dùng đo xong ở ResultScreen, tự động lưu thông số này vào Database.

## 2. Màn hình Lịch sử (`HistoryScreen.kt`)
* **Giao diện:** 
  - Một danh sách (LazyColumn) hiển thị tất cả các lần đo trước đó.
  - Nhóm các lần đo theo ngày (Today, Yesterday...).
  - Mỗi item trong danh sách hiển thị: Con số BPM, Icon trạng thái (Tim màu xanh/đỏ tuỳ thuộc vào BPM), thời gian đo.

## 3. Biểu đồ Đường (Line Chart) trên Màn hình Home
* **Vị trí:** Thay thế/Nâng cấp phần khoảng trống trên màn hình `HomeScreen`.
* **Tính năng:**
  - Vẽ biểu đồ đường (Line Chart) xu hướng nhịp tim trong 7 ngày gần nhất.
  - Dùng đường cong (Bezier curve) mềm mại.
  - Nếu có quá ít dữ liệu, hiển thị placeholder "Đo thêm để xem xu hướng sức khoẻ".

## 4. Màn hình Hồ sơ (`ProfileScreen.kt`)
* **Tính năng:** 
  - Cho phép người dùng nhập Tuổi, Chiều cao, Cân nặng.
  - Dựa vào Tuổi để tính toán Mức Nhịp tim Tối đa (Max BPM = 220 - Tuổi).
  - Giao diện có nút "Dark Mode / Light Mode".

---
*Lưu ý cho AI: Tiếp tục áp dụng `AUTO_WORKFLOW.md` sau khi hoàn thành từng Task của Epic này.*