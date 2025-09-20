# Khởi động 
docker-compose up -d postgres redis minio

# Kiểm tra trạng thái
docker-compose ps

# ----------------------------------------------------
# Truy cập Swagger UI:  
URL: http://localhost:8080/swagger-ui.html
# API Docs: 
http://localhost:8080/api-docs

# ------------------LINUX----------------------------------
## Xem dung lượng disk
df -h

## Xem dung lượng ram
free -h

## Xem thông tin real time
htop