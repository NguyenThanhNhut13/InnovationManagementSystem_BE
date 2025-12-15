package vn.edu.iuh.fit.innovationmanagementsystem_be.integration.minio;

import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FileService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DisplayName("FileService Integration Tests")
@SuppressWarnings("resource")
class FileServiceIntegrationTest {

    @Container
    static MinIOContainer minioContainer = new MinIOContainer(
            DockerImageName.parse("minio/minio:latest"))
            .withUserName("minioadmin")
            .withPassword("minioadmin");

    private FileService fileService;
    private MinioClient minioClient;
    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() throws Exception {
        // Create MinIO client
        minioClient = MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials("minioadmin", "minioadmin")
                .build();

        // Create bucket if not exists
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
        }

        // Create FileService and inject dependencies
        fileService = new FileService(minioClient);
        ReflectionTestUtils.setField(fileService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(fileService, "publicEndpoint", minioContainer.getS3URL());
        ReflectionTestUtils.setField(fileService, "accessKey", "minioadmin");
        ReflectionTestUtils.setField(fileService, "secretKey", "minioadmin");
    }

    // ==================== Upload Tests ====================

    @Test
    @DisplayName("1. Upload Bytes - Should upload file to MinIO successfully")
    void testUploadBytes_Success() {
        byte[] content = "Test file content for MinIO upload".getBytes(StandardCharsets.UTF_8);
        String originalFilename = "test-document.pdf";
        String contentType = "application/pdf";

        String uploadedFileName = fileService.uploadBytes(content, originalFilename, contentType);

        assertNotNull(uploadedFileName);
        assertTrue(uploadedFileName.endsWith(".pdf"));
        assertTrue(fileService.fileExists(uploadedFileName));
    }

    @Test
    @DisplayName("2. Upload Bytes With Name - Should upload with fixed filename")
    void testUploadBytesWithName_Success() {
        byte[] content = "Backup content data".getBytes(StandardCharsets.UTF_8);
        String fileName = "backup/2024/backup-file.sql";
        String contentType = "application/sql";

        String uploadedFileName = fileService.uploadBytesWithName(content, fileName, contentType);

        assertEquals(fileName, uploadedFileName);
        assertTrue(fileService.fileExists(fileName));
    }

    // ==================== Download Tests ====================

    @Test
    @DisplayName("3. Download File - Should download file from MinIO")
    void testDownloadFile_Success() throws Exception {
        String testContent = "Content to be downloaded from MinIO";
        byte[] content = testContent.getBytes(StandardCharsets.UTF_8);
        String uploadedFileName = fileService.uploadBytes(content, "download-test.txt", "text/plain");

        InputStream downloadedStream = fileService.downloadFile(uploadedFileName);

        assertNotNull(downloadedStream);
        String downloadedContent = new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(testContent, downloadedContent);
        downloadedStream.close();
    }

    // ==================== Delete Tests ====================

    @Test
    @DisplayName("4. Delete File - Should remove file from MinIO")
    void testDeleteFile_Success() throws Exception {
        byte[] content = "File to be deleted".getBytes(StandardCharsets.UTF_8);
        String uploadedFileName = fileService.uploadBytes(content, "delete-test.txt", "text/plain");
        assertTrue(fileService.fileExists(uploadedFileName));

        fileService.deleteFile(uploadedFileName);

        assertFalse(fileService.fileExists(uploadedFileName));
    }

    // ==================== File Exists Tests ====================

    @Test
    @DisplayName("5. File Exists - Should return true for existing file")
    void testFileExists_True() {
        byte[] content = "Existing file content".getBytes(StandardCharsets.UTF_8);
        String uploadedFileName = fileService.uploadBytes(content, "exists-test.txt", "text/plain");

        boolean exists = fileService.fileExists(uploadedFileName);

        assertTrue(exists);
    }

    @Test
    @DisplayName("6. File Exists - Should return false for non-existent file")
    void testFileExists_False() {
        boolean exists = fileService.fileExists("non-existent-file.txt");

        assertFalse(exists);
    }

    @Test
    @DisplayName("7. File Exists - Should return false for null/empty filename")
    void testFileExists_NullOrEmpty() {
        assertFalse(fileService.fileExists(null));
        assertFalse(fileService.fileExists(""));
        assertFalse(fileService.fileExists("   "));
    }

    // ==================== Get File Info Tests ====================

    @Test
    @DisplayName("8. Get File Info - Should return file metadata")
    void testGetFileInfo_Success() throws Exception {
        byte[] content = "Content for file info test".getBytes(StandardCharsets.UTF_8);
        String uploadedFileName = fileService.uploadBytes(content, "info-test.txt", "text/plain");

        StatObjectResponse fileInfo = fileService.getFileInfo(uploadedFileName);

        assertNotNull(fileInfo);
        assertEquals(content.length, fileInfo.size());
        assertEquals(BUCKET_NAME, fileInfo.bucket());
        assertEquals(uploadedFileName, fileInfo.object());
    }

    // ==================== Presigned URL Tests ====================

    @Test
    @DisplayName("9. Get Presigned URL - Should generate valid presigned URL")
    void testGetPresignedUrl_Success() {
        byte[] content = "Content for presigned URL test".getBytes(StandardCharsets.UTF_8);
        String uploadedFileName = fileService.uploadBytes(content, "presigned-test.txt", "text/plain");

        String presignedUrl = fileService.getPresignedUrl(uploadedFileName, 3600);

        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.contains(BUCKET_NAME));
        assertTrue(presignedUrl.contains(uploadedFileName));
    }

    // ==================== Test MinIO Connection ====================

    @Test
    @DisplayName("10. Test MinIO Connection - Should verify bucket and connection")
    void testMinioConnection_Success() {
        assertDoesNotThrow(() -> fileService.testMinioConnection());
    }
}
