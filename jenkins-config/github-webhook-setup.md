# GitHub Webhook Setup cho Auto-Deploy

Để có **tự động deploy ngay lập tức** khi push code lên nhánh `main`, bạn cần cấu hình GitHub Webhook.

## 🚀 **Cách 1: GitHub Webhook (Khuyến nghị - Deploy ngay lập tức)**

### **Bước 1: Cài đặt GitHub Plugin trong Jenkins**

1. Truy cập Jenkins: http://localhost:8081
2. Vào **Manage Jenkins** → **Manage Plugins**
3. Tab **Available** → Tìm và cài đặt:
   - ✅ **GitHub plugin**
   - ✅ **GitHub Branch Source plugin**
   - ✅ **GitHub API plugin**

### **Bước 2: Cấu hình GitHub Integration**

1. Vào **Manage Jenkins** → **Configure System**
2. Tìm section **GitHub**
3. Click **Add GitHub Server**:
   - **Name**: `GitHub`
   - **API URL**: `https://api.github.com`
   - **Credentials**: Chọn GitHub credentials đã tạo
   - ✅ **Manage hooks**: Checked
4. Click **Test connection** để kiểm tra
5. **Save**

### **Bước 3: Tạo Pipeline Job với GitHub Integration**

1. **New Item** → **Pipeline**
2. **Name**: `Innovation-Management-System`
3. **Pipeline** section:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/NguyenThanhNhut13/InnovationManagementSystem_BE.git`
   - **Credentials**: Chọn GitHub credentials
   - **Branches to build**: `*/main`
   - **Script Path**: `Jenkinsfile`

4. **Build Triggers** section:
   - ✅ **GitHub hook trigger for GITScm polling**

5. **Save**

### **Bước 4: Cấu hình GitHub Webhook**

1. Vào GitHub repository: `https://github.com/NguyenThanhNhut13/InnovationManagementSystem_BE`
2. **Settings** → **Webhooks** → **Add webhook**
3. Cấu hình:
   - **Payload URL**: `http://your-jenkins-ip:8081/github-webhook/`
   - **Content type**: `application/json`
   - **Which events**: ✅ **Just the push event**
   - **Active**: ✅ Checked
4. **Add webhook**

## 🔄 **Cách 2: SCM Polling (Hiện tại - Kiểm tra mỗi 2 phút)**

Nếu không muốn setup webhook, Jenkins sẽ kiểm tra repository mỗi 2 phút:

```xml
<triggers>
    <hudson.triggers.SCMTrigger>
        <spec>H/2 * * * *</spec>  <!-- Mỗi 2 phút -->
    </hudson.triggers.SCMTrigger>
</triggers>
```

## 📋 **Luồng Auto-Deploy**

### **Khi push lên nhánh `main`:**
1. **GitHub Webhook** → Jenkins (ngay lập tức)
2. **Jenkins Pipeline** chạy:
   - ✅ **Checkout** code
   - ✅ **Build & Test** (Unit tests, Code quality)
   - ✅ **Package** (Maven build)
   - ✅ **Docker Build** (Tạo image)
   - ✅ **Security Scan** (Trivy scan)
   - ✅ **Deploy to Production** (Tự động deploy)

### **Khi push lên nhánh `develop`:**
1. **GitHub Webhook** → Jenkins
2. **Jenkins Pipeline** chạy:
   - ✅ Tất cả stages như trên
   - ✅ **Deploy to Staging** (port 8082)

## 🎯 **Cấu Hình Branch Strategy**

```groovy
// Trong Jenkinsfile
stage('Deploy to Staging') {
    when {
        branch 'develop'  // Chỉ chạy khi push develop
    }
    // Deploy to staging
}

stage('Deploy to Production') {
    when {
        branch 'main'     // Chỉ chạy khi push main
    }
    // Deploy to production
}
```

## 🔧 **Troubleshooting**

### **Webhook không hoạt động:**
1. Kiểm tra Jenkins có thể truy cập từ internet không
2. Kiểm tra firewall/port 8081
3. Kiểm tra GitHub webhook delivery logs
4. Kiểm tra Jenkins logs: `docker logs jenkins`

### **Pipeline không trigger:**
1. Kiểm tra GitHub credentials
2. Kiểm tra repository URL
3. Kiểm tra branch configuration
4. Kiểm tra webhook URL

### **Deploy thất bại:**
1. Kiểm tra Docker daemon
2. Kiểm tra network connectivity
3. Kiểm tra environment variables
4. Kiểm tra container logs

## 📊 **Monitoring**

### **Jenkins Dashboard:**
- Xem build history
- Xem build logs
- Xem deployment status

### **GitHub Integration:**
- Xem commit status
- Xem build results trong GitHub
- Xem deployment status

## 🚨 **Lưu Ý Bảo Mật**

- **Không expose** Jenkins ra internet nếu không cần thiết
- **Sử dụng HTTPS** trong production
- **Restrict webhook** chỉ từ GitHub
- **Monitor** webhook deliveries
- **Rotate** credentials định kỳ

## ✅ **Kiểm Tra Setup**

1. **Push code lên nhánh `main`**
2. **Kiểm tra Jenkins** có tự động build không
3. **Kiểm tra deployment** có thành công không
4. **Kiểm tra application** có chạy trên port 8080 không

Với cấu hình này, mỗi khi bạn push code lên nhánh `main`, Jenkins sẽ **tự động deploy** lên production!
