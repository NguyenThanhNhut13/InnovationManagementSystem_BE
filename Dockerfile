# --- Stage 1: Build ứng dụng bằng Maven ---
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Copy source vào container
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build jar (bỏ qua test để nhanh hơn)
RUN mvn clean package -DskipTests

# --- Stage 2: Chạy ứng dụng bằng JDK nhẹ ---
FROM eclipse-temurin:17-jre-alpine

# Set timezone (optional)
ENV TZ=Asia/Ho_Chi_Minh

# Tạo thư mục app
WORKDIR /app

# Copy jar từ stage build sang
COPY --from=builder /app/target/*.jar app.jar

# Expose port (ví dụ app chạy ở 8080)
EXPOSE 8080

# Lệnh chạy khi container start
ENTRYPOINT ["java","-jar","/app/app.jar"]
