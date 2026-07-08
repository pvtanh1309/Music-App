# Music-App DevOps & Cloud Infrastructure Documentation

Tài liệu này đóng vai trò là hướng dẫn (guide) cho các thành viên mới và là tài liệu lưu trữ kiến trúc hệ thống, quy trình triển khai (deploy) và vận hành của dự án Music-App.

## 1. Tổng quan Dự án (Project Overview)

Đây là một dự án Web phát nhạc trực tuyến full-stack chạy trên cloud, cho phép người dùng nghe nhạc và quản lý danh sách nhạc yêu thích của mình. Dự án được xây dựng dựa trên các công nghệ hiện đại và được thiết kế để có thể mở rộng và dễ dàng bảo trì nhờ vào việc áp dụng DevOps và Cloud Infrastructure giúp triển khai nhanh chóng.
- **Frontend (FE):** `MusicApp_FE/`
- **Backend (BE):** `MusicApp_BE/`

## 2. Tech Stack

### Backend (`MusicApp_BE`)
- **Ngôn ngữ / Framework:** Java Spring Boot
- **Port mặc định:** 8080
- **Lệnh cài đặt thư viện & build:** `mvn clean package`
- **Lệnh khởi chạy (Local Dev):** `./mvnw spring-boot:run` (Nếu đã có sẵn Java trên máy, nếu chưa có thì chạy bằng Docker)
- **Biến môi trường cần thiết (.env):** Đọc file example và điền thông tin vào .env

### Database & Redis
- **DATABASE**: Postgresql:18.4 (Bitnami)
- **REDIS**: Redis:8.8.0 (Bitnami)
- **Sử dụng docker compose:** `docker compose -f compose.replication.yml up -d --build`


### Frontend (`MusicApp_FE`)
- **Ngôn ngữ / Framework:** Typesript + Vite 
- **Lệnh cài đặt thư viện:** `npm install`
- **Lệnh build production:** `npm run build`
- **Lệnh khởi chạy (Local Dev):** `npm run dev`

## 3. Containerization (Docker)
- Hướng dẫn build image cho FE: `docker build -t music-fe:latest -f Dockerfile .`
- Hướng dẫn build image cho BE: `docker build -t music-be:latest -f Dockerfile .`
- Lệnh chạy toàn bộ hệ thống bằng Docker Compose: `docker compose -f compose.yml up -d --build`

## 4. Infrastructure as Code (IaC)
- Công cụ sử dụng: Terraform
- Cấu trúc thư mục IaC: `...`
- Lệnh deploy hạ tầng: `terraform apply`

## 5. CI/CD Pipelines
- Luồng chạy tự động khi có code mới: Build -> Test -> Deploy
- Đường dẫn file cấu hình pipeline: `...`

## 6. Môi trường Triển khai (AWS)
- **Frontend được host tại:** `...`
- **Backend được host tại:** `...`
- **Database:** `...`


<!-- Lưu ý:
1. Mật khẩu MAIL_PASSWORD trong .env là mật khẩu app password chứ không phải mật khẩu tài khoản gmail. Để lấy được mật khẩu này thì thực hiện làm theo các bước sau:
- Vào Email bật mật khẩu 2 lớp
- Sau khi bật suy cập: https://myaccount.google.com/apppasswords
- Tên ứng dụng có thể đặt tùy ý (Ví dụ: Music App)
- Sau khi bấm tạo app password thì sẽ có 16 ký tự xuất hiện, đây chính là mật khẩu MAIL_PASSWORD trong .env (bỏ dấu cách nếu có)

2. Đăng nhập lần đầu sẽ phải đăng ký.

3. Quy tắc mạng trong Docker Compose: Các container sẽ nói chuyện với nhau bằng tên của service (Không dùng IP)
Ví dụ: App truy cập Database PostgreSQL theo địa chỉ Host là "postgres" vì postgres chạy trên Docker chứ không phải localhost

4. nginx (đã được gắn vào container frontend) giao tiếp với app qua network musicapp-network
-->


