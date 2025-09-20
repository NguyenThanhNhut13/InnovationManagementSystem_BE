# Jenkins Credentials Setup for Private Repository

Vì project của bạn là **private repository**, bạn cần cấu hình credentials trong Jenkins để có thể truy cập vào repository.

## 🔐 **Cách 1: Sử dụng Personal Access Token (Khuyến nghị)**

### **Bước 1: Tạo Personal Access Token trên GitHub**

1. Đăng nhập vào GitHub
2. Vào **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
3. Click **Generate new token** → **Generate new token (classic)**
4. Điền thông tin:
   - **Note**: `Jenkins CI/CD Access`
   - **Expiration**: Chọn thời gian hết hạn (khuyến nghị 90 days)
   - **Scopes**: Chọn ít nhất:
     - ✅ `repo` (Full control of private repositories)
     - ✅ `read:org` (Read org and team membership)
5. Click **Generate token**
6. **Copy token** và lưu lại (chỉ hiển thị 1 lần)

### **Bước 2: Cấu hình Credentials trong Jenkins**

1. Truy cập Jenkins: http://localhost:8081
2. Đăng nhập với:
   - **Username**: `admin`
   - **Password**: `Quinton@443`
3. Vào **Manage Jenkins** → **Manage Credentials**
4. Click **System** → **Global credentials (unrestricted)**
5. Click **Add Credentials**
6. Điền thông tin:
   - **Kind**: `Username with password`
   - **Username**: `NguyenThanhNhut13` (GitHub username)
   - **Password**: `[Paste Personal Access Token]`
   - **ID**: `github-credentials`
   - **Description**: `GitHub Private Repository Access`
7. Click **OK**

## 🔐 **Cách 2: Sử dụng SSH Key**

### **Bước 1: Tạo SSH Key**

```bash
# Tạo SSH key pair
ssh-keygen -t rsa -b 4096 -C "your-email@example.com"

# Copy public key
cat ~/.ssh/id_rsa.pub
```

### **Bước 2: Thêm SSH Key vào GitHub**

1. Vào GitHub → **Settings** → **SSH and GPG keys**
2. Click **New SSH key**
3. Paste public key và save

### **Bước 3: Cấu hình trong Jenkins**

1. Vào **Manage Jenkins** → **Manage Credentials**
2. **Add Credentials**:
   - **Kind**: `SSH Username with private key`
   - **Username**: `git`
   - **Private Key**: `Enter directly` → Paste private key
   - **ID**: `github-ssh-credentials`

## 🔧 **Cập nhật Jenkinsfile cho Private Repository**

Nếu sử dụng SSH, cập nhật Jenkinsfile:

```groovy
stage('Checkout') {
    steps {
        echo 'Checking out source code...'
        checkout([
            $class: 'GitSCM',
            branches: [[name: '*/main']],
            userRemoteConfigs: [[
                url: 'git@github.com:NguyenThanhNhut13/InnovationManagementSystem_BE.git',
                credentialsId: 'github-ssh-credentials'
            ]]
        ])
    }
}
```

## ✅ **Kiểm tra Cấu hình**

1. Tạo Pipeline Job mới
2. Cấu hình:
   - **Pipeline script from SCM**: Git
   - **Repository URL**: `https://github.com/NguyenThanhNhut13/InnovationManagementSystem_BE.git`
   - **Credentials**: Chọn credentials đã tạo
   - **Branch**: `*/main`
3. Click **Save** và **Build Now**

## 🚨 **Lưu ý Bảo mật**

- **Không commit** Personal Access Token vào code
- **Không share** credentials với người khác
- **Rotate** token định kỳ
- Sử dụng **least privilege principle** cho token permissions

## 🔍 **Troubleshooting**

### **Lỗi: "Repository not found"**
- Kiểm tra repository URL
- Kiểm tra credentials có đúng không
- Kiểm tra token có quyền truy cập repository

### **Lỗi: "Authentication failed"**
- Kiểm tra username/password
- Kiểm tra token chưa hết hạn
- Kiểm tra token có đúng scopes

### **Lỗi: "Permission denied"**
- Kiểm tra SSH key đã được thêm vào GitHub
- Kiểm tra SSH key có đúng format không
