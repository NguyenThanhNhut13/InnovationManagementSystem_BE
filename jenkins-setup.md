# Hướng dẫn thiết lập Jenkins CI/CD

## Bước 1: Khởi động các services

```bash
docker-compose up -d
```

## Bước 2: Lấy mật khẩu admin Jenkins

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## Bước 3: Truy cập Jenkins

Mở trình duyệt: http://localhost:8080

- Nhập mật khẩu admin từ bước 2
- Chọn "Install suggested plugins"
- Tạo tài khoản admin

## Bước 4: Cài đặt Docker plugin

1. Vào **Manage Jenkins** → **Manage Plugins**
2. Tab **Available**, tìm và cài:
   - Docker Pipeline
   - GitHub Integration Plugin
3. Restart Jenkins

## Bước 5: Tạo Jenkins Pipeline Job

1. Click **New Item**
2. Nhập tên: `InnovationManagement-BE-Deploy`
3. Chọn **Pipeline** → OK
4. Trong **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/NguyenThanhNhut13/InnovationManagementSystem_BE`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
5. Save

## Bước 6: Test deployment trên LOCAL (Manual Build)

Đây là cách test đơn giản nhất, không cần webhook:

### Manual build:
1. Vào job → Click **Build Now**
2. Xem logs trong **Console Output**
3. Kiểm tra xem có lỗi gì không

**Lưu ý:** Mỗi lần push code mới lên GitHub, bạn vào Jenkins click **Build Now** để deploy.

## Bước 7: Thiết lập Auto-deploy với GitHub Webhook (Khi deploy lên VPS)

⚠️ **Chỉ làm bước này khi bạn đã deploy lên VPS có IP public!**

### Cách 1: Sử dụng ngrok để test webhook trên local (Tạm thời)

1. Tải ngrok: https://ngrok.com/download
2. Chạy ngrok:
   ```bash
   ngrok http 8080
   ```
3. Copy URL ngrok (ví dụ: `https://abc123.ngrok.io`)
4. Vào GitHub repo → **Settings** → **Webhooks** → **Add webhook**
5. Payload URL: `https://abc123.ngrok.io/github-webhook/`
6. Content type: `application/json`
7. Chọn: **Just the push event**
8. Add webhook

### Cách 2: Khi deploy lên VPS (Production)

1. Vào GitHub repo → **Settings** → **Webhooks** → **Add webhook**
2. Payload URL: `http://YOUR_VPS_IP:8080/github-webhook/`
3. Content type: `application/json`
4. Chọn: **Just the push event**
5. Add webhook

### Trong Jenkins (Cho cả 2 cách):
1. Vào job `InnovationManagement-BE-Deploy`
2. **Configure**
3. **Build Triggers** → Check **GitHub hook trigger for GITScm polling**
4. Save

## Bước 8: Test auto-deploy

### Test manual build (Local):
1. Vào job → Click **Build Now**
2. Xem logs trong **Console Output**
3. Nếu thành công, kiểm tra app tại http://localhost:8081

### Test auto build (Sau khi setup webhook):
1. Sửa code bất kỳ (ví dụ: thêm comment)
2. Commit và push lên nhánh `main`
3. Jenkins sẽ tự động build và deploy
4. Xem logs trong Jenkins để kiểm tra

## Kiểm tra ứng dụng

- Backend API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- Jenkins: http://localhost:8080
- MinIO Console: http://localhost:9001

## Troubleshooting

### Nếu Jenkins không thể build Docker:
```bash
docker exec -u root jenkins chmod 666 /var/run/docker.sock
```

### Xem logs container:
```bash
docker logs innovation-backend -f
```

### Restart services:
```bash
docker-compose restart app
```

### Xóa và rebuild:
```bash
docker-compose down
docker-compose up -d --build
```
