# Jenkins Environment Variables Setup

## Tổng quan
Vì file `.env` không được commit lên GitHub, chúng ta cần setup các environment variables trong Jenkins để thay thế.

## Cách 1: Setup trong Jenkins Global Environment Variables

### Bước 1: Vào Jenkins Global Configuration
1. Đăng nhập Jenkins với tài khoản admin
2. Vào **Manage Jenkins** → **Configure System**
3. Scroll xuống phần **Global properties**
4. Tick vào **Environment variables**
5. Thêm các biến môi trường sau:

### Bước 2: Thêm Environment Variables

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/innovation_management
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=InnovationDB2024!Secure
SPRING_DATASOURCE_SSL=false
SPRING_DATASOURCE_SSLMODE=disable

# Redis Configuration
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_USERNAME=
SPRING_DATA_REDIS_PASSWORD=Redis2024!SecureCache
SPRING_DATA_REDIS_DATABASE=0
SPRING_DATA_REDIS_SSL=false

# Email Configuration
SPRING_MAIL_HOST=localhost
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=test@example.com
SPRING_MAIL_PASSWORD=test
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# MinIO Configuration
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=MinIO2024!SecureStorage
MINIO_BUCKET_NAME=innovation-management

# JWT Configuration
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=86400000

# Server Configuration
PORT=8080
```

### Bước 3: Lưu Configuration
Click **Save** để lưu cấu hình.

## Cách 2: Setup trong Job-specific Environment Variables

### Bước 1: Vào Job Configuration
1. Vào job **Innovation-Management-System**
2. Click **Configure**
3. Scroll xuống phần **Build Environment**
4. Tick vào **Use secret text(s) or file(s)**

### Bước 2: Thêm Credentials
1. Click **Add** → **Secret text**
2. Thêm từng secret một:

**Database Password:**
- Secret: `InnovationDB2024!Secure`
- Variable: `DB_PASSWORD`

**Redis Password:**
- Secret: `Redis2024!SecureCache`
- Variable: `REDIS_PASSWORD`

**MinIO Secret Key:**
- Secret: `MinIO2024!SecureStorage`
- Variable: `MINIO_SECRET_KEY`

## Cách 3: Sử dụng Jenkinsfile với Environment Variables

### Cập nhật Jenkinsfile để sử dụng environment variables:

```groovy
pipeline {
    agent any
    
    environment {
        // Database Configuration
        DB_HOST = 'postgres'
        DB_PORT = '5432'
        DB_NAME = 'innovation_management'
        DB_USER = 'postgres'
        DB_PASSWORD = credentials('db-password') // Jenkins credential
        
        // Redis Configuration
        REDIS_HOST = 'redis'
        REDIS_PORT = '6379'
        REDIS_PASSWORD = credentials('redis-password') // Jenkins credential
        
        // MinIO Configuration
        MINIO_ENDPOINT = 'http://minio:9000'
        MINIO_ACCESS_KEY = 'minioadmin'
        MINIO_SECRET_KEY = credentials('minio-secret-key') // Jenkins credential
        
        // Email Configuration
        SPRING_MAIL_HOST = 'localhost'
        SPRING_MAIL_PORT = '587'
        SPRING_MAIL_USERNAME = 'test@example.com'
        SPRING_MAIL_PASSWORD = 'test'
        
        // Maven Configuration
        MAVEN_OPTS = '-Xmx1024m'
    }
    
    // ... rest of pipeline
}
```

## Cách 4: Sử dụng Docker Secrets (Khuyến nghị cho Production)

### Tạo Docker secrets:
```bash
# Tạo secrets
echo "InnovationDB2024!Secure" | docker secret create db_password -
echo "Redis2024!SecureCache" | docker secret create redis_password -
echo "MinIO2024!SecureStorage" | docker secret create minio_secret_key -
```

### Sử dụng trong docker-compose.yml:
```yaml
services:
  backend:
    image: innovation-management-system-be:latest
    secrets:
      - db_password
      - redis_password
      - minio_secret_key
    environment:
      - SPRING_DATASOURCE_PASSWORD_FILE=/run/secrets/db_password
      - SPRING_DATA_REDIS_PASSWORD_FILE=/run/secrets/redis_password
      - MINIO_SECRET_KEY_FILE=/run/secrets/minio_secret_key

secrets:
  db_password:
    external: true
  redis_password:
    external: true
  minio_secret_key:
    external: true
```

## Khuyến nghị

1. **Development/Testing**: Sử dụng Cách 1 (Global Environment Variables)
2. **Production**: Sử dụng Cách 4 (Docker Secrets)
3. **CI/CD**: Sử dụng Cách 3 (Jenkinsfile với credentials)

## Lưu ý bảo mật

- Không bao giờ commit file `.env` lên GitHub
- Sử dụng Jenkins credentials cho các thông tin nhạy cảm
- Rotate passwords định kỳ
- Sử dụng different passwords cho different environments
