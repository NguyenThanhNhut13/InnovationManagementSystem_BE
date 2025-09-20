# Quick Start Guide - Innovation Management System với Jenkins CI/CD

## 🚀 Khởi động nhanh

### Trên Windows:
```cmd
# Khởi động tất cả services (bao gồm Jenkins)
start-services.bat
```

### Trên Linux/Mac:
```bash
# Khởi động tất cả services
docker-compose up -d
```

## 📋 Services và Ports

| Service | Port | URL | Credentials |
|---------|------|-----|-------------|
| Backend | 8080 | http://localhost:8080 | - |
| Jenkins | 8081 | http://localhost:8081 | admin / admin123 |
| MinIO Console | 9001 | http://localhost:9001 | minioadmin / MinIO2024!SecureStorage |

## 🔄 CI/CD Pipeline

Jenkins sẽ tự động:
- **Build** Docker image khi có push vào `main` branch
- **Deploy** tự động lên production

### Cách sử dụng Jenkins:
1. Truy cập http://localhost:8081
2. Login: admin / admin123
3. Tạo Pipeline job mới
4. Chọn "Pipeline script from SCM"
5. Chọn Git và nhập URL repository
6. Script Path: Jenkinsfile
7. Save và Run

## 🐳 Docker Commands

```bash
# Khởi động tất cả
docker-compose up -d

# Xem logs
docker-compose logs -f

# Dừng tất cả
docker-compose down

# Restart service cụ thể
docker-compose restart backend
```

## 🛠️ Troubleshooting

### Services không khởi động:
```bash
# Kiểm tra Docker
docker --version
docker-compose --version

# Kiểm tra logs
docker-compose logs [service-name]
```

### Jenkins không truy cập được:
- Kiểm tra Jenkins logs: `docker-compose logs jenkins`
- Đảm bảo Jenkins đã khởi động hoàn toàn (có thể mất 1-2 phút)

---

**Lưu ý**: Đây là setup đơn giản cho development/testing.
