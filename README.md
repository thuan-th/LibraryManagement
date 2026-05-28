# 📚 Online Bookstore Management System
Một ứng dụng web cho phép người dùng tìm kiếm và mua sách trực tuyến. Quản trị viên có thể quản lý sách, đơn hàng, người dùng và xem thống kê doanh thu theo tháng hoặc năm.

## 🛠️ Công Nghệ Sử Dụng
- **Backend:** Java, Spring Boot
- **Frontend:** Thymeleaf, HTML, CSS, JavaScript
- **CSDL:** MySQL

## 👥 Role
- **Quản trị viên (Admin):**
    - Quản lý sách, người dùng, đơn hàng
    - Xem thống kê doanh thu theo tháng/năm

- **Khách hàng (User):**
    - Đăng ký, đăng nhập
    - Tìm sách, thêm vào giỏ hàng và đặt hàng
    - Xem lịch sử đơn hàng

## ✨ Chức Năng Chính

### 📚 Quản Lý Sách (Admin)
- Thêm/sửa/xoá sách và danh mục sách (CRUD)

### 📊 Thống Kê Doanh Thu (Admin)
- Xem báo cáo doanh thu theo tháng và năm

### 🛒 Giỏ Hàng & Đặt Hàng (User)
- Thêm/xoá sách khỏi giỏ hàng
- Thanh toán và xác nhận đơn hàng
- Xem lịch sử đơn hàng

## ⚙️ Hướng Dẫn Chạy Chương Trình

### Yêu Cầu Trước Khi Chạy
- Java 17
- MySQL đã cài đặt và khởi động

### Các Bước Cài Đặt

1. **Clone dự án:**
   ```bash
   git clone https://github.com/tht250502/Library
   ```

   ```bash
   cd Library
   ```
   
2. **Tạo CSDL MySQL**
   ```sql
   CREATE DATABASE library;
   ```
   
3. **Cấu hình file ```application.properties```**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/library
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.datasource.username=[mysql_username]
   spring.datasource.password=[mysql_password]
   # Thay username và password của bạn
   spring.jpa.hibernate.ddl-auto=update
   ```
   
4. **Chạy ứng dụng**
   ```bash
   mvn spring-boot:run
   ```

5. **Truy cập địa chỉ**
   ```
   https://localhost:8080
   ```
6. **Tài khoản test**
   ```
   User
   TK: user@test.com
   MK: 12345
   ```

   ```
   Admin
   TK: admin@test.com
   MK: 12345
   ```
   

