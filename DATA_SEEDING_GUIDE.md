# Data Seeding Guide - Hệ thống Quản lý Sáng kiến

## 🌱 **Tổng quan Data Seeding**

Hệ thống được tích hợp **Data Seeding** tự động để khởi tạo dữ liệu mặc định khi chạy ứng dụng lần đầu tiên. Các lần chạy tiếp theo sẽ **bỏ qua** việc seeding để tránh tạo dữ liệu trùng lặp.

## 🎯 **Dữ liệu được khởi tạo**

### 1. **Departments (Khoa/Viện) - 17 đơn vị**

| **Mã Khoa** | **Tên Khoa/Viện** |
|-------------|-------------------|
| `CNCK` | Khoa Công nghệ Cơ khí |
| `CND` | Khoa Công nghệ Điện |
| `CNDT` | Khoa Công nghệ Điện tử |
| `CNDL` | Khoa Công nghệ Động Lực |
| `CNHH` | Khoa Công nghệ Hóa học |
| `CNMTT` | Khoa Công nghệ May - Thời trang |
| `CNNL` | Khoa Công nghệ Nhiệt - Lạnh |
| `CNTT` | Khoa Công nghệ Thông tin |
| `KTKT` | Khoa Kế toán - Kiểm toán |
| `KTXD` | Khoa Kỹ thuật Xây dựng |
| `LUAT` | Khoa Luật |
| `NN` | Khoa Ngoại ngữ |
| `QTKD` | Khoa Quản trị Kinh doanh |
| `TCNH` | Khoa Tài chính - Ngân hàng |
| `TMDL` | Khoa Thương mại du lịch |
| `VCNSHTP` | Viện Công nghệ Sinh học và Thực phẩm |
| `VKHCNQLMT` | Viện Khoa học Công nghệ và Quản lý Môi trường |

### 2. **Admin User**

| **Field** | **Value** |
|-----------|-----------|
| **Personnel ID** | `ADMIN001` |
| **Full Name** | `Quản trị viên Hệ thống` |
| **Email** | `admin@iuh.edu.vn` |
| **Password** | `admin123` |
| **Phone** | `0123456789` |
| **Role** | `QUAN_TRI_VIEN` |
| **Department** | `Khoa Công nghệ Thông tin` |

### 3. **Sample Users (6 người)**

| **Personnel ID** | **Full Name** | **Email** | **Role** | **Department** | **Password** |
|------------------|---------------|-----------|----------|----------------|--------------|
| `GV001` | Nguyễn Văn An | nguyenvanan@iuh.edu.vn | GIANG_VIEN | CNTT | 123456 |
| `TK001` | Trần Thị Bình | tranthibinh@iuh.edu.vn | THU_KY_KHOA | CNTT | 123456 |
| `TK002` | Lê Minh Cường | leminhcuong@iuh.edu.vn | TRUONG_KHOA | CNTT | 123456 |
| `GV002` | Phạm Thị Dung | phamthidung@iuh.edu.vn | GIANG_VIEN | QTKD | 123456 |
| `TK003` | Hoàng Văn Em | hoangvanem@iuh.edu.vn | THU_KY_KHOA | QTKD | 123456 |
| `GV003` | Vũ Thị Phương | vuthiphuong@iuh.edu.vn | GIANG_VIEN | KTKT | 123456 |

## 🚀 **Cách thức hoạt động**

### 1. **Automatic Seeding**
```java
@Component
public class DataSeeder implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        seedDepartments();    // Tạo 17 khoa/viện
        seedAdminUser();      // Tạo admin user
        seedSampleUsers();    // Tạo 6 sample users
    }
}
```

### 2. **Smart Checking**
- **Departments**: Kiểm tra `departmentRepository.count() > 0`
- **Admin User**: Kiểm tra `userRepository.existsByEmail("admin@iuh.edu.vn")`
- **Sample Users**: Kiểm tra `userRepository.count() > 1`

### 3. **Logging**
```
2024-01-01 10:00:00 INFO  DataSeeder - Seeding initial department data...
2024-01-01 10:00:01 INFO  DataSeeder - Created department: CNTT - Khoa Công nghệ Thông tin
2024-01-01 10:00:02 INFO  DataSeeder - Department data seeding completed. Total departments created: 17
2024-01-01 10:00:03 INFO  DataSeeder - Creating default admin user...
2024-01-01 10:00:04 INFO  DataSeeder - Created admin user: ADMIN001 - admin@iuh.edu.vn
2024-01-01 10:00:05 INFO  DataSeeder - Default admin credentials: admin@iuh.edu.vn / admin123
```

## 🧪 **Testing với dữ liệu mẫu**

### 1. **Đăng nhập Admin**
```json
POST /api/v1/auth/login
{
    "emailOrPersonnelId": "admin@iuh.edu.vn",
    "password": "admin123"
}
```

### 2. **Đăng nhập Sample User**
```json
POST /api/v1/auth/login
{
    "emailOrPersonnelId": "nguyenvanan@iuh.edu.vn",
    "password": "123456"
}
```

### 3. **Test Department APIs**
```json
GET /api/v1/departments
// Sẽ trả về 17 departments

GET /api/v1/departments/search?name=công nghệ
// Tìm kiếm departments có từ "công nghệ"
```

### 4. **Test User APIs**
```json
GET /api/v1/users
// Sẽ trả về 7 users (1 admin + 6 sample users)

GET /api/v1/users/role/GIANG_VIEN
// Sẽ trả về 3 giảng viên
```

## 🔄 **Data Reset**

### Cách xóa dữ liệu để chạy lại seeding:

#### **Option 1: Database Reset**
```sql
-- Xóa tất cả dữ liệu
DELETE FROM users;
DELETE FROM departments;

-- Hoặc drop tables
DROP TABLE users;
DROP TABLE departments;
```

#### **Option 2: Application Properties**
```properties
# Recreate database schema on startup
spring.jpa.hibernate.ddl-auto=create-drop
```

#### **Option 3: Selective Reset**
```sql
-- Chỉ xóa users để test user seeding
DELETE FROM users;

-- Chỉ xóa departments để test department seeding
DELETE FROM departments;
```

## ⚙️ **Configuration**

### 1. **Enable/Disable Seeding**
```java
// Trong DataSeeder.java, comment out methods không muốn chạy
@Override
public void run(String... args) throws Exception {
    seedDepartments();
    // seedAdminUser();      // Disable admin seeding
    // seedSampleUsers();    // Disable sample user seeding
}
```

### 2. **Customize Data**
```java
// Thay đổi dữ liệu trong DataSeeder.java
List<DepartmentData> departmentDataList = Arrays.asList(
    new DepartmentData("Khoa mới", "KMOI"),
    // Thêm departments mới...
);
```

### 3. **Environment-specific Seeding**
```java
@Profile("dev")  // Chỉ chạy trong dev environment
@Component
public class DataSeeder implements CommandLineRunner {
    // ...
}
```

## 🔒 **Security Notes**

### ⚠️ **Production Warnings**
- **Đổi password admin** trước khi deploy production
- **Xóa sample users** trong production
- **Disable seeding** trong production environment

### ✅ **Best Practices**
```java
// Production-ready admin creation
if (!isProductionEnvironment()) {
    seedAdminUser();
    seedSampleUsers();
}
```

## 📋 **Benefits**

### ✅ **Development**
- **Instant Setup**: Không cần tạo dữ liệu thủ công
- **Consistent Data**: Tất cả developers có cùng dữ liệu test
- **Quick Testing**: Có sẵn users và departments để test APIs

### ✅ **Testing**
- **Automated**: Tự động có dữ liệu cho integration tests
- **Predictable**: Dữ liệu test không thay đổi
- **Complete**: Cover tất cả roles và departments

### ✅ **Demo**
- **Ready-to-show**: Hệ thống luôn có dữ liệu để demo
- **Realistic**: Dữ liệu phản ánh thực tế IUH
- **Professional**: Giao diện không trống rỗng

**Data Seeding giúp hệ thống sẵn sàng hoạt động ngay từ lần chạy đầu tiên!** 🎉
