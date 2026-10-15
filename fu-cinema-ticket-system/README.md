# FU Cinema Ticket System — `fu-cinema-ticket-system`

Hệ thống quản lý và bán vé xem phim trực tuyến xây dựng bằng **Spring Boot & Spring Data JPA** cho môn học **HSF302**.

## 1. Công nghệ sử dụng
- **Java:** 21+
- **Spring Boot:** 4.1.1 (hoặc 3.3.x)
- **Maven:** 3.9+ (hoặc Maven Wrapper `mvnw`)
- **Database:** Microsoft SQL Server (database: `FUCinemaTicketDB`), H2 cho test/dev ban đầu
- **ORM:** Spring Data JPA / Hibernate
- **Template Engine:** Thymeleaf
- **Validation:** Spring Boot Starter Validation
- **Security Utility:** Spring Security Crypto (`BCryptPasswordEncoder`)
- **Khác:** Lombok, Spring Boot DevTools

## 2. Hướng dẫn khởi chạy dự án

### Yêu cầu môi trường:
- JDK 21 trở lên (đã cấu hình `JAVA_HOME`).
- Maven 3.9+ (hoặc dùng `./mvnw` / `mvnw.cmd`).

### Chạy bằng Maven Wrapper:
```bash
# Windows Command Prompt / PowerShell:
.\mvnw.cmd spring-boot:run

# Linux / macOS / Bash:
./mvnw spring-boot:run
```

Truy cập hệ thống tại: `http://localhost:8080`
