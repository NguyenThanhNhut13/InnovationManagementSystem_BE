# Quick Start - Test Jenkins CI/CD trên Local

## Bước 1: Khởi động services

```cmd
docker-compose up -d
```

Đợi khoảng 1-2 phút để các services khởi động.

## Bước 2: Lấy mật khẩu Jenkins

```cmd
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Copy mật khẩu này.

## Bước 3: Setup Jenkins

1. Mở trình duyệt: **http://localhost:8080**
2. Paste mật khẩu từ bước 2
3. Click **Install suggested plugins** (đợi cài đặt xong)
4. Tạo tài khoản admin:
   - Username: admin
   - Password: (tự đặt)
   - Full name: Admin
   - Email: admin@example.com
5. Click **Save and Continue** → **Save and Finish** → **Start using Jenkins**

## Bước 4: Cài thêm Docker plugin

1. **Manage Jenkins** → **Manage Plugins**
2. Tab **Available plugins**
3. Tìm và check:
   - **Docker Pipeline**
   - **Docker plugin**
4. Click **Install without restart**
5. Đợi cài xong

## Bước 5: Tạo Pipeline Job

1. Click **New Item** (góc trái)
2. Nhập tên: `InnovationManagement-BE-Deploy`
3. Chọn **Pipeline**
4. Click **OK**
5. Kéo xuống phần **Pipeline**:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/NguyenThanhNhut13/InnovationManagementSystem_BE`
   - Branch Specifier: `*/main`
   - Script Path: `Jenkinsfile`
6. Click **Save**

## Bước 6: Test Build

1. Click **Build Now** (bên trái)
2. Xem progress trong **Build History**
3. Click vào build number (ví dụ: #1)
4. Click **Console Output** để xem logs

**Lưu ý:** Build lần đầu sẽ mất 5-10 phút vì phải download Maven dependencies.

## Bước 7: Kiểm tra kết quả

Nếu build thành công:

```cmd
docker ps
```

Bạn sẽ thấy container `innovation-backend` đang chạy.

Kiểm tra app:
- API: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui.html
- Health: http://localhost:8081/actuator/health

## Test Auto-deploy (Không cần webhook)

Jenkins đã được cấu hình để tự động check GitHub mỗi 5 phút.

**Cách test:**
1. Sửa code bất kỳ (ví dụ: thêm comment trong file README.md)
2. Commit và push lên GitHub:
   ```cmd
   git add .
   git commit -m "test jenkins auto deploy"
   git push origin main
   ```
3. Đợi tối đa 5 phút
4. Vào Jenkins, bạn sẽ thấy build mới tự động chạy

**Hoặc build ngay lập tức:**
- Vào Jenkins → Click **Build Now**

## Troubleshooting

### Lỗi: Cannot connect to Docker daemon

```cmd
docker exec -u root jenkins chmod 666 /var/run/docker.sock
```

Sau đó build lại.

### Xem logs container

```cmd
docker logs innovation-backend -f
```

### Restart Jenkins

```cmd
docker restart jenkins
```

### Xóa và build lại từ đầu

```cmd
docker-compose down -v
docker-compose up -d
```

## Khi deploy lên VPS

1. Copy toàn bộ project lên VPS
2. Chạy `docker-compose up -d`
3. Setup GitHub webhook với IP VPS
4. Tắt polling trong Jenkinsfile (xóa dòng `pollSCM`)

Xem chi tiết trong file `jenkins-setup.md`
