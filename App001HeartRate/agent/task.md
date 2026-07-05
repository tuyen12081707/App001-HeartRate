# Task: Refactor Add Record & Result Screens

## Objective
Xây dựng lại luồng Thêm nhịp tim thủ công (Add Record) và màn hình Hiển thị kết quả (Result Screen). Yêu cầu UI/UX phải hiện đại, có animation mượt mà, và Validate chặt chẽ trước khi chuyển trang.

## 1. Màn hình Add Record (`AddRecordScreen.kt`)
* **BPM Input (Trọng tâm):** KHÔNG dùng `TextField` nhập số nhàm chán. Hãy tự custom một `WheelNumberPicker` (Cuộn số) bằng `LazyColumn` kết hợp với `rememberSnapFlingBehavior` của Compose. 
  - Dải số cho phép cuộn: từ `40` đến `220` BPM.
  - Số đang được chọn ở giữa phải to lên (scale lớn hơn) và đổi màu Đỏ chủ đạo, các số xung quanh mờ đi (alpha giảm).
* **Các input khác:** - Trạng thái đo (Resting, Exercising, After Wake up...): Hiển thị dạng `FilterChip` hoặc các nút bo tròn nằm ngang (Row) để user bấm chọn nhanh.
* **Validation Logic:**
  - Nút "Xác nhận / Xem Kết Quả" phải bị `disabled` (mờ đi, không cho bấm) nếu user chưa chọn đủ thông tin (chưa có BPM, chưa chọn Trạng thái).
  - Khi hợp lệ, nút bấm sáng lên. Bấm vào sẽ lưu vào Database (gọi ViewModel) và điều hướng (Navigate) sang màn Result, truyền theo ID hoặc chỉ số BPM vừa đo.

## 2. Màn hình Result (`ResultScreen.kt`)
* **Hero Section (Nổi bật nhất):** - Ở chính giữa màn hình trên cùng, tạo một vòng tròn to (hoặc hình trái tim) có viền Gradient (Đỏ - Cam).
  - Sử dụng `Animatable` hoặc `animateIntAsState` để tạo hiệu ứng số đếm nhảy từ 0 lên con số BPM thực tế khi màn hình vừa mở lên.
* **Health Status Indicator:**
  - Dựa vào chỉ số BPM, hiển thị text đánh giá tình trạng.
  - Ví dụ: 60-100 -> "Bình thường" (màu Xanh lá). Dưới 60 -> "Hơi thấp" (Màu Xanh dương). Trên 100 -> "Hơi cao" (Màu Cam/Đỏ).
* **Details Card:** - Một `Card` bo góc 24dp (Glassmorphism hoặc shadow nhẹ) hiển thị lại Ngày giờ đo và Trạng thái đo.
* **Action Buttons:** - 2 nút ở dưới cùng: Nút Primary (Đỏ) "Về Trang Chủ", Nút Secondary (Outlined) "Đo lại/Thêm mới".