---
name: app001heartrate
description: Skill chuyên biệt cho dự án App001HeartRate. Cung cấp các SOP (Standard Operating Procedures) cho AI tự động setup code API, UI, Database theo chuẩn của dự án.
---

# Kịch bản phát triển cho App001HeartRate

Skill này chứa các kịch bản cụ thể cho AI. Khi làm việc với dự án **App001HeartRate**, hãy tự động làm theo các bước dưới đây để thiết lập code chuẩn xác.

## 1. Yêu cầu: "Tạo Call API cho tính năng [Tên tính năng]"
Khi người dùng yêu cầu tạo Call API:
1. **Models**: Tạo Data Class (hoặc Serializable class) cho Request và Response trong thư mục của tính năng tương ứng ở `App001HeartRate/commonMain/.../data/models`. Đảm bảo sử dụng `@Serializable` (kotlinx.serialization).
2. **API Client**: Tạo một interface/class định nghĩa HTTP Call sử dụng **Ktor** HttpClient trong `App001HeartRate/commonMain/.../data/remote`. 
3. **Repository**: Tạo Repository interface trong `App001HeartRate/commonMain/.../domain/repository` và class implement nó trong `App001HeartRate/commonMain/.../data/repository`. Hàm call API phải là `suspend function`.
4. **DI (Dependency Injection)**: Nếu có dùng Koin, hãy tự động sinh code cập nhật module DI để provide Repository vừa tạo.

## 2. Yêu cầu: "Tạo màn hình/UI cho [Tên tính năng]"
Khi người dùng yêu cầu tạo giao diện:
1. **ViewModel/State**: Tạo một ViewModel trong `App001HeartRate/commonMain` sử dụng `StateFlow` để quản lý UI State và các Event.
2. **UI Compose**: Tạo file giao diện `@Composable` trong thư mục UI của `App001HeartRate/commonMain`. Lắng nghe State từ ViewModel và truyền Event ngược lại.
3. **Màn hình Native (Nếu không dùng Compose Multiplatform)**:
   - Viết `@Composable` trong `App001HeartRate/androidMain`.
   - Hướng dẫn hoặc sinh code SwiftUI cho `App001HeartRate/iosMain`.

## 3. Yêu cầu: "Tính toán nhịp tim / Tương tác Native"
1. Logic thuật toán nhịp tim: Viết toàn bộ bằng Kotlin thuần trong `App001HeartRate/commonMain/.../domain/usecase`.
2. Truy cập Cảm biến/Bluetooth/Camera: 
   - Viết `expect class/function` ở `commonMain`.
   - Sinh code `actual class/function` ở `androidMain` và `iosMain`.

## Quy tắc chung (Agent Rules)
- Tự động thực hiện các bước trên mà không cần hỏi lại.
- **KHÔNG** sử dụng thư viện riêng của Android (như `android.util.Log`) trong `commonMain`.
- Luôn đặt code đúng vào thư mục của module `App001HeartRate`, không viết lung tung ra ngoài root `KMP`.
