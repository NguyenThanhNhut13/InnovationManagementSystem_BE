package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

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
            ensureBucketExists();

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new Exception("File size exceeds 10MB limit");
            }

            // Generate unique file name
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFileName = generateUniqueFileName(fileExtension);

            // Tạo file trong MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return uniqueFileName;

        } catch (Exception e) {
            throw new IdInvalidException("Failed to upload file: " + e.getMessage());
        }
    }

    // 3. Upload raw bytes (ví dụ PDF tạo từ server) lên MinIO
    public String uploadBytes(byte[] data, String originalFilename, String contentType) {
        try {
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

            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(inputStream, data.length, -1)
                            .contentType(contentType)
                            .build());

            return uniqueFileName;
        } catch (Exception e) {
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
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            return true;
        } catch (Exception e) {
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
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
            }
        } catch (Exception e) {
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
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(expirySeconds)
                            .build());
        } catch (Exception e) {
            throw new IdInvalidException("Fail to view file: " + e.getMessage());
        }
    }
}
