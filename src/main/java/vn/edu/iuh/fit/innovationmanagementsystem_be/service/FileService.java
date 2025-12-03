package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-endpoint:http://minio:9000}")
    private String publicEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    // 1. Upload multiple files to MinIO
    public List<String> uploadMultipleFiles(List<MultipartFile> files) throws Exception {
        List<String> uploadedFileNames = new ArrayList<>();

        try {
            ensureBucketExists();

            long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
            if (totalSize > 50 * 1024 * 1024) { // 50MB limit
                throw new Exception("Total request size exceeds 50MB limit");
            }

            // Upload each file
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = uploadFile(file);
                    uploadedFileNames.add(fileName);
                }
            }

            return uploadedFileNames;

        } catch (Exception e) {
            throw new IdInvalidException("Failed to upload files: " + e.getMessage());
        }
    }

    // 2. Upload file to MinIO
    public String uploadFile(MultipartFile file) throws Exception {
        try {
            log.info("Bắt đầu upload file: originalFilename={}, size={}, contentType={}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            ensureBucketExists();
            log.info("Bucket '{}' đã được đảm bảo tồn tại", bucketName);

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new Exception("File size exceeds 10MB limit");
            }

            // Generate unique file name
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFileName = generateUniqueFileName(fileExtension);
            log.info("Tên file unique được tạo: {}", uniqueFileName);

            // Tạo file trong MinIO
            log.info("Đang upload file lên MinIO: bucket={}, object={}", bucketName, uniqueFileName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            log.info("Upload file thành công: {}", uniqueFileName);

            // Verify file đã tồn tại trên MinIO (với retry để tránh race condition)
            boolean exists = false;
            int retryCount = 0;
            int maxRetries = 3;
            while (!exists && retryCount < maxRetries) {
                try {
                    Thread.sleep(100 * retryCount); // Tăng delay mỗi lần retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                exists = fileExists(uniqueFileName);
                retryCount++;
                if (!exists && retryCount < maxRetries) {
                    log.warn("File chưa tồn tại, đang retry lần {}: {}", retryCount, uniqueFileName);
                }
            }
            if (!exists) {
                log.error("CẢNH BÁO: File vừa upload không tồn tại trên MinIO sau {} lần thử: {}", maxRetries,
                        uniqueFileName);
                throw new IdInvalidException("File upload thành công nhưng không tìm thấy trên MinIO");
            }
            log.info("Xác nhận file tồn tại trên MinIO: {}", uniqueFileName);

            return uniqueFileName;

        } catch (Exception e) {
            log.error("Lỗi khi upload file: {}", e.getMessage(), e);
            throw new IdInvalidException("Failed to upload file: " + e.getMessage());
        }
    }

    // 3. Upload raw bytes (ví dụ PDF tạo từ server) lên MinIO
    public String uploadBytes(byte[] data, String originalFilename, String contentType) {
        try {
            log.info("Bắt đầu upload bytes: originalFilename={}, size={}, contentType={}",
                    originalFilename, data != null ? data.length : 0, contentType);

            ensureBucketExists();

            if (data == null || data.length == 0) {
                throw new IdInvalidException("File content rỗng");
            }

            if (data.length > 10 * 1024 * 1024) {
                throw new IdInvalidException("File size exceeds 10MB limit");
            }

            String fileExtension = getFileExtension(originalFilename);
            if (fileExtension == null || fileExtension.isBlank()) {
                fileExtension = ".pdf";
            }
            String uniqueFileName = generateUniqueFileName(fileExtension);
            log.info("Tên file unique được tạo: {}", uniqueFileName);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

            log.info("Đang upload bytes lên MinIO: bucket={}, object={}", bucketName, uniqueFileName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(inputStream, data.length, -1)
                            .contentType(contentType)
                            .build());
            log.info("Upload bytes thành công: {}", uniqueFileName);

            // Verify file đã tồn tại trên MinIO (với retry để tránh race condition)
            boolean exists = false;
            int retryCount = 0;
            int maxRetries = 3;
            while (!exists && retryCount < maxRetries) {
                try {
                    Thread.sleep(100 * retryCount); // Tăng delay mỗi lần retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                exists = fileExists(uniqueFileName);
                retryCount++;
                if (!exists && retryCount < maxRetries) {
                    log.warn("File chưa tồn tại, đang retry lần {}: {}", retryCount, uniqueFileName);
                }
            }
            if (!exists) {
                log.error("CẢNH BÁO: File vừa upload không tồn tại trên MinIO sau {} lần thử: {}", maxRetries,
                        uniqueFileName);
                throw new IdInvalidException("File upload thành công nhưng không tìm thấy trên MinIO");
            }
            log.info("Xác nhận file tồn tại trên MinIO: {}", uniqueFileName);

            return uniqueFileName;
        } catch (Exception e) {
            log.error("Lỗi khi upload bytes: {}", e.getMessage(), e);
            throw new IdInvalidException("Failed to upload file: " + e.getMessage());
        }
    }

    // 4. Download file from MinIO
    public InputStream downloadFile(String fileName) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            throw new IdInvalidException("Failed to download file: " + e.getMessage());
        }
    }

    // 5. Xóa file trong MinIO
    public void deleteFile(String fileName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            throw new IdInvalidException("Failed to delete file: " + e.getMessage());
        }
    }

    // 6. Kiểm tra nếu file tồn tại trong MinIO
    public boolean fileExists(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            log.warn("fileExists: fileName is null or empty");
            return false;
        }
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            return true;
        } catch (Exception e) {
            log.debug("File không tồn tại hoặc lỗi khi kiểm tra: fileName={}, error={}", fileName, e.getMessage());
            return false;
        }
    }

    // 7. Lấy thông tin file từ MinIO
    public StatObjectResponse getFileInfo(String fileName) throws Exception {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            throw new IdInvalidException("Failed to get file info: " + e.getMessage());
        }
    }

    /**
     * Ensure bucket exists, create if not
     */
    private void ensureBucketExists() throws Exception {
        try {
            log.info("Kiểm tra bucket tồn tại: {}", bucketName);
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());

            if (!bucketExists) {
                log.info("Bucket '{}' không tồn tại, đang tạo mới...", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
                log.info("Đã tạo bucket thành công: {}", bucketName);

                // Verify lại sau khi tạo
                boolean verified = minioClient.bucketExists(
                        BucketExistsArgs.builder()
                                .bucket(bucketName)
                                .build());
                if (!verified) {
                    log.error("Lỗi: Không thể xác nhận bucket đã được tạo: {}", bucketName);
                    throw new IdInvalidException("Không thể tạo bucket: " + bucketName);
                }
                log.info("Xác nhận bucket đã được tạo thành công: {}", bucketName);
            } else {
                log.info("Bucket '{}' đã tồn tại", bucketName);
            }
        } catch (Exception e) {
            log.error("Lỗi khi đảm bảo bucket tồn tại: bucket={}, error={}", bucketName, e.getMessage(), e);
            throw new IdInvalidException("Failed to ensure bucket exists: " + e.getMessage());
        }
    }

    /**
     * Generate unique file name with timestamp and UUID
     */
    private String generateUniqueFileName(String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + fileExtension;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    public String getPresignedUrl(String fileName, int expirySeconds) {
        try {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IdInvalidException("File name is required");
            }
            // Loại bỏ bucket name prefix nếu có (ví dụ:
            // "innovation-management/20251122_xxx.pdf" -> "20251122_xxx.pdf")
            String objectName = fileName;
            if (fileName.startsWith(bucketName + "/")) {
                objectName = fileName.substring(bucketName.length() + 1);
            }

            // Parse publicEndpoint để lấy hostname, port và secure flag
            URL url = new URL(publicEndpoint);
            String host = url.getHost();
            int originalPort = url.getPort();
            int port = originalPort == -1 ? (url.getProtocol().equals("https") ? 443 : 80) : originalPort;
            boolean secure = url.getProtocol().equals("https");

            // Tạo client với public endpoint để signature đúng ngay từ đầu
            MinioClient publicClient = MinioClient.builder()
                    .endpoint(host, port, secure)
                    .credentials(accessKey, secretKey)
                    .build();

            // Tạo presigned URL với public endpoint
            String presignedUrl = publicClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expirySeconds)
                            .build());
            return presignedUrl;
        } catch (Exception e) {
            throw new IdInvalidException("Fail to view file: " + e.getMessage());
        }
    }

    // 8. Kiểm tra kết nối MinIO và thông tin bucket
    public void testMinioConnection() {
        try {
            log.info("=== Bắt đầu kiểm tra kết nối MinIO ===");
            log.info("Bucket name: {}", bucketName);
            log.info("Public endpoint: {}", publicEndpoint);
            log.info("Access key: {}***",
                    accessKey != null && accessKey.length() > 3 ? accessKey.substring(0, 3) : "N/A");

            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
            log.info("Bucket tồn tại: {}", bucketExists);

            if (bucketExists) {
                // List objects trong bucket
                Iterable<Result<Item>> results = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucketName)
                                .build());
                int count = 0;
                for (Result<Item> result : results) {
                    Item item = result.get();
                    log.info("Object trong bucket: {} (size: {})", item.objectName(), item.size());
                    count++;
                    if (count >= 10) {
                        log.info("... và nhiều hơn");
                        break;
                    }
                }
                log.info("Tổng số object trong bucket (hiển thị tối đa 10): {}", count);
            }

            log.info("=== Kết thúc kiểm tra kết nối MinIO ===");
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra kết nối MinIO: {}", e.getMessage(), e);
            throw new IdInvalidException("Failed to test MinIO connection: " + e.getMessage());
        }
    }
}
