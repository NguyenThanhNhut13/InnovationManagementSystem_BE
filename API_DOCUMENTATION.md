# API Documentation - Hệ thống Quản lý Sáng kiến

## Tổng quan
Hệ thống cung cấp các API để quản lý người dùng, xác thực và phân quyền trong hệ thống quản lý sáng kiến.

## Base URL
```
http://localhost:8080
```

## Authentication
Hệ thống sử dụng JWT (JSON Web Token) để xác thực. Token cần được gửi trong header:
```
Authorization: Bearer <token>
```

## API Endpoints

### 1. Authentication APIs

#### 1.1 Đăng nhập
- **URL:** `POST /api/v1/auth/login`
- **Description:** Đăng nhập vào hệ thống
- **Request Body:**
```json
{
    "emailOrPersonnelId": "user@example.com hoặc NV001",
    "password": "password123"
}
```
- **Response:**
```json
{
    "success": true,
    "message": "Đăng nhập thành công",
    "data": {
        "accessToken": "jwt_access_token_here",
        "refreshToken": "refresh_token_here",
        "userId": "user_id",
        "fullName": "Họ và tên",
        "email": "user@example.com",
        "role": "GIANG_VIEN",
        "departmentName": "Tên khoa",
        "expiresIn": 86400
    },
    "statusCode": 200
}
```

#### 1.2 Đăng ký
- **URL:** `POST /api/v1/auth/register`
- **Description:** Đăng ký tài khoản mới
- **Request Body:**
```json
{
    "personnelId": "NV001",
    "fullName": "Họ và tên",
    "email": "user@example.com",
    "phoneNumber": "0123456789",
    "password": "password123",
    "role": "GIANG_VIEN",
    "departmentId": "dept_id"
}
```
- **Response:** Tương tự như đăng nhập

#### 1.3 Làm mới token
- **URL:** `POST /api/v1/auth/refresh`
- **Description:** Làm mới JWT token với refresh token rotation
- **Request Body:**
```json
{
    "refreshToken": "refresh_token_here"
}
```
- **Response:**
```json
{
    "success": true,
    "message": "Làm mới token thành công",
    "data": {
        "accessToken": "new_jwt_access_token",
        "refreshToken": "new_refresh_token",
        "expiresIn": 86400
    },
    "statusCode": 200
}
```

#### 1.4 Đăng xuất
- **URL:** `POST /api/v1/auth/logout`
- **Description:** Đăng xuất và vô hiệu hóa refresh token hiện tại
- **Request Body:**
```json
{
    "refreshToken": "refresh_token_here"
}
```

#### 1.5 Đăng xuất tất cả thiết bị
- **URL:** `POST /api/v1/auth/logout-all`
- **Description:** Đăng xuất khỏi tất cả thiết bị (vô hiệu hóa tất cả refresh token)
- **Headers:** `Authorization: Bearer <token>`

#### 1.6 Đổi mật khẩu
- **URL:** `POST /api/v1/auth/change-password`
- **Description:** Đổi mật khẩu người dùng
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
```json
{
    "oldPassword": "old_password",
    "newPassword": "new_password",
    "confirmPassword": "new_password"
}
```

#### 1.7 Lấy thông tin profile
- **URL:** `GET /api/v1/auth/profile`
- **Description:** Lấy thông tin profile của người dùng hiện tại
- **Headers:** `Authorization: Bearer <token>`

### 2. User Management APIs

#### 2.1 Lấy danh sách người dùng
- **URL:** `GET /api/v1/users`
- **Description:** Lấy tất cả người dùng trong hệ thống
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
```json
{
    "success": true,
    "message": "Lấy danh sách người dùng thành công",
    "data": [
        {
            "id": "user_id",
            "personnelId": "NV001",
            "fullName": "Họ và tên",
            "email": "user@example.com",
            "phoneNumber": "0123456789",
            "role": "GIANG_VIEN",
            "departmentId": "dept_id",
            "departmentName": "Tên khoa",
            "createdAt": "2024-01-01T00:00:00",
            "updatedAt": "2024-01-01T00:00:00"
        }
    ],
    "statusCode": 200
}
```

#### 2.2 Lấy thông tin người dùng theo ID
- **URL:** `GET /api/v1/users/{id}`
- **Description:** Lấy thông tin chi tiết của một người dùng
- **Headers:** `Authorization: Bearer <token>`

#### 2.3 Tạo người dùng mới
- **URL:** `POST /api/v1/users`
- **Description:** Tạo người dùng mới (chỉ dành cho admin)
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
```json
{
    "personnelId": "NV002",
    "fullName": "Người dùng mới",
    "email": "newuser@example.com",
    "phoneNumber": "0987654321",
    "password": "password123",
    "role": "GIANG_VIEN",
    "departmentId": "dept_id"
}
```

#### 2.4 Cập nhật thông tin người dùng
- **URL:** `PUT /api/v1/users/{id}`
- **Description:** Cập nhật thông tin người dùng
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
```json
{
    "fullName": "Tên mới",
    "email": "newemail@example.com",
    "phoneNumber": "0111222333",
    "role": "THU_KY_KHOA",
    "departmentId": "new_dept_id"
}
```

#### 2.5 Lấy người dùng theo vai trò
- **URL:** `GET /api/v1/users/role/{role}`
- **Description:** Lấy danh sách người dùng theo vai trò
- **Headers:** `Authorization: Bearer <token>`
- **Vai trò có sẵn:** `GIANG_VIEN`, `THU_KY_KHOA`, `TRUONG_KHOA`, `TV_HOI_DONG_KHOA`, `THU_KY_QLKH_HTQT`, `TV_HOI_DONG_TRUONG`, `CHU_TICH_HD_TRUONG`, `QUAN_TRI_VIEN`

#### 2.6 Lấy người dùng theo khoa
- **URL:** `GET /api/v1/users/department/{departmentId}`
- **Description:** Lấy danh sách người dùng theo khoa/viện
- **Headers:** `Authorization: Bearer <token>`

#### 2.7 Tìm kiếm người dùng
- **URL:** `GET /api/v1/users/search?fullName={name}`
- **Description:** Tìm kiếm người dùng theo tên (không phân biệt hoa thường)
- **Headers:** `Authorization: Bearer <token>`

#### 2.8 Xóa người dùng
- **URL:** `DELETE /api/v1/users/{id}`
- **Description:** Xóa người dùng
- **Headers:** `Authorization: Bearer <token>`

#### 2.9 Kiểm tra email tồn tại
- **URL:** `GET /api/v1/users/check-email/{email}`
- **Description:** Kiểm tra email đã được sử dụng chưa
- **Headers:** `Authorization: Bearer <token>`

#### 2.10 Kiểm tra mã nhân viên tồn tại
- **URL:** `GET /api/v1/users/check-personnel-id/{personnelId}`
- **Description:** Kiểm tra mã nhân viên đã được sử dụng chưa
- **Headers:** `Authorization: Bearer <token>`

### 3. Department Management APIs

#### 3.1 Lấy danh sách khoa/viện
- **URL:** `GET /api/v1/departments`
- **Description:** Lấy tất cả khoa/viện trong hệ thống
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
```json
{
    "success": true,
    "message": "Lấy danh sách khoa/viện thành công",
    "data": [
        {
            "id": "dept_id",
            "departmentName": "Khoa Công nghệ Thông tin",
            "departmentCode": "CNTT",
            "totalUsers": 25,
            "totalInnovations": 12
        }
    ],
    "statusCode": 200
}
```

#### 3.2 Lấy thông tin khoa/viện theo ID
- **URL:** `GET /api/v1/departments/{id}`
- **Description:** Lấy thông tin chi tiết của một khoa/viện
- **Headers:** `Authorization: Bearer <token>`

#### 3.3 Tạo khoa/viện mới
- **URL:** `POST /api/v1/departments`
- **Description:** Tạo khoa/viện mới
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
```json
{
    "departmentName": "Khoa Quản trị Kinh doanh",
    "departmentCode": "QTKD"
}
```
- **Validation:**
  - `departmentName`: Bắt buộc, tối đa 255 ký tự
  - `departmentCode`: Bắt buộc, tối đa 50 ký tự, phải unique

#### 3.4 Cập nhật khoa/viện
- **URL:** `PUT /api/v1/departments/{id}`
- **Description:** Cập nhật thông tin khoa/viện
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:** Tương tự như tạo khoa/viện

#### 3.5 Xóa khoa/viện
- **URL:** `DELETE /api/v1/departments/{id}`
- **Description:** Xóa khoa/viện (chỉ khi không có người dùng hoặc sáng kiến)
- **Headers:** `Authorization: Bearer <token>`

#### 3.6 Tìm kiếm khoa/viện
- **URL:** `GET /api/v1/departments/search?name={name}`
- **Description:** Tìm kiếm khoa/viện theo tên (không phân biệt hoa thường)
- **Headers:** `Authorization: Bearer <token>`

#### 3.7 Kiểm tra mã khoa/viện tồn tại
- **URL:** `GET /api/v1/departments/check-code/{code}`
- **Description:** Kiểm tra mã khoa/viện đã được sử dụng chưa
- **Headers:** `Authorization: Bearer <token>`

## Response Format

Tất cả API đều trả về response theo format:
```json
{
    "success": true/false,
    "message": "Thông báo",
    "data": {...},
    "statusCode": 200
}
```

## Error Codes

- `200`: Thành công
- `400`: Bad Request - Dữ liệu không hợp lệ
- `401`: Unauthorized - Chưa xác thực hoặc token không hợp lệ
- `403`: Forbidden - Không có quyền truy cập
- `404`: Not Found - Không tìm thấy tài nguyên
- `500`: Internal Server Error - Lỗi hệ thống

## Cài đặt và chạy

1. **Cài đặt dependencies:**
```bash
mvn clean install
```

2. **Cấu hình database:**
- Cập nhật thông tin database trong `application.properties`
- Đảm bảo PostgreSQL đang chạy

3. **Chạy ứng dụng:**
```bash
mvn spring-boot:run
```

4. **Truy cập API:**
- Base URL: `http://localhost:8080`
- Swagger UI (nếu có): `http://localhost:8080/swagger-ui.html`

## JWT Configuration

### RSA-256 Algorithm
- Hệ thống sử dụng RSA-256 cho JWT signing
- Private key để sign token, Public key để verify
- RSA key pair được generate tự động khi khởi động ứng dụng

### Token Management
- **Access Token**: Thời hạn 1 ngày (86400 giây)
- **Refresh Token**: Thời hạn 7 ngày, lưu trong Redis
- **Refresh Token Rotation**: Mỗi lần refresh sẽ tạo token mới và vô hiệu hóa token cũ
- **Multi-device Support**: Hỗ trợ đăng nhập nhiều thiết bị

### Redis Integration
- Refresh tokens được lưu trong Redis với TTL tự động
- Key format: `refresh_token:{token}` và `user_refresh_tokens:{userId}`
- Automatic cleanup khi token hết hạn

## Validation Rules

### User Fields
- **personnelId**: Bắt buộc, tối đa 50 ký tự, phải unique
- **fullName**: Bắt buộc, tối đa 255 ký tự
- **email**: Bắt buộc, định dạng email hợp lệ, phải unique
- **phoneNumber**: Định dạng: bắt đầu bằng 0, 10-11 số
- **password**: Tối thiểu 6 ký tự (khi tạo mới)

### Department Fields
- **departmentName**: Bắt buộc, tối đa 255 ký tự
- **departmentCode**: Bắt buộc, tối đa 50 ký tự, phải unique

## Business Rules

### User Management
- Không thể xóa user nếu có ràng buộc dữ liệu
- Email và mã nhân viên phải unique trong hệ thống
- Password được mã hóa bằng BCrypt
- Cập nhật user không thay đổi personnelId và password

### Department Management
- Không thể xóa department nếu còn user hoặc innovation
- Department code phải unique
- Khi cập nhật, phải kiểm tra code trùng với department khác

## Lưu ý

- Tất cả API (trừ `/api/v1/auth/**` và `/api/public/**`) đều yêu cầu xác thực
- Đăng nhập hỗ trợ cả email và mã nhân viên
- Access token có thời hạn 1 ngày (86400 giây)
- Refresh token có thời hạn 7 ngày, tự động xóa khỏi Redis khi hết hạn
- Mật khẩu được mã hóa bằng BCrypt
- Hệ thống hỗ trợ CORS cho tất cả origin
- Tất cả API đều có comprehensive error handling và validation
- Search endpoints hỗ trợ tìm kiếm không phân biệt hoa thường
