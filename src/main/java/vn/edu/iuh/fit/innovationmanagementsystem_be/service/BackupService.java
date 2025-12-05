package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.BackupHistory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupStatus;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.BackupRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.BackupResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.BackupHistoryRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final BackupHistoryRepository backupHistoryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final EmailService emailService;
    private final MinioClient minioClient;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private static final String BACKUP_FOLDER = "backups/";
    private static final long MAX_EMAIL_ATTACHMENT_SIZE = 25 * 1024 * 1024; // 25MB

    // 1. Tạo backup
    @Transactional
    public BackupResponse createBackup(BackupRequest request) {
        log.info("Bắt đầu tạo backup với loại: {}", request.backupType());

        User currentUser = getCurrentUser();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String baseFileName = BACKUP_FOLDER + request.backupType().name().toLowerCase() + "_backup_" + timestamp;

        BackupHistory backupHistory = new BackupHistory();
        backupHistory.setBackupType(request.backupType());
        backupHistory.setStatus(BackupStatus.IN_PROGRESS);
        backupHistory.setCreatedByUser(currentUser);
        backupHistory.setDescription(request.description());
        backupHistory.setFileName(baseFileName);
        backupHistory = backupHistoryRepository.save(backupHistory);

        try {
            byte[] backupData;
            String fileName;

            switch (request.backupType()) {
                case DATABASE -> {
                    backupData = backupDatabase();
                    fileName = baseFileName + ".sql";
                }
                case FILES -> {
                    backupData = backupMinioFiles();
                    fileName = baseFileName + "_files.zip";
                }
                case FULL -> {
                    backupData = createFullBackup(baseFileName);
                    fileName = baseFileName + "_full.zip";
                }
                default -> throw new IdInvalidException("Loại backup không hợp lệ: " + request.backupType());
            }

            String uploadedFileName = fileService.uploadBytesWithName(backupData, fileName, "application/octet-stream");

            backupHistory.setFileName(uploadedFileName);
            backupHistory.setFileSize((long) backupData.length);
            backupHistory.setStatus(BackupStatus.SUCCESS);
            backupHistory.setCompletedAt(LocalDateTime.now());
            backupHistoryRepository.save(backupHistory);

            if (request.sendEmail() && backupData.length <= MAX_EMAIL_ATTACHMENT_SIZE) {
                String recipientEmail = (request.email() != null && !request.email().isBlank())
                        ? request.email()
                        : currentUser.getEmail();
                sendBackupEmail(recipientEmail, uploadedFileName, backupData, request.backupType());
            } else if (request.sendEmail()) {
                log.warn("File backup quá lớn ({} bytes), không thể gửi qua email. Giới hạn: {} bytes",
                        backupData.length, MAX_EMAIL_ATTACHMENT_SIZE);
            }

            log.info("Backup thành công: {}", uploadedFileName);
            return toBackupResponse(backupHistory, "Backup thành công!");

        } catch (Exception e) {
            log.error("Lỗi khi tạo backup: {}", e.getMessage(), e);
            backupHistory.setStatus(BackupStatus.FAILED);
            backupHistory.setErrorMessage(e.getMessage());
            backupHistory.setCompletedAt(LocalDateTime.now());
            backupHistoryRepository.save(backupHistory);
            throw new IdInvalidException("Lỗi khi tạo backup: " + e.getMessage());
        }
    }

    // 2. Restore backup từ file upload (multipart/form-data)
    @Transactional
    public BackupResponse restoreFromUpload(MultipartFile file, BackupType backupType) {
        log.info("Bắt đầu restore từ file upload: {}, loại: {}", file.getOriginalFilename(), backupType);

        try {
            byte[] backupData = file.getBytes();

            switch (backupType) {
                case DATABASE -> restoreDatabase(backupData);
                case FILES -> restoreMinioFiles(backupData);
                case FULL -> restoreFullBackup(backupData);
                default -> throw new IdInvalidException("Loại restore không hợp lệ: " + backupType);
            }

            log.info("Restore thành công từ file upload: {}", file.getOriginalFilename());

            return new BackupResponse(
                    null,
                    backupType,
                    file.getOriginalFilename(),
                    file.getSize(),
                    BackupStatus.SUCCESS,
                    "Restore thành công từ file upload!",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    null,
                    null);

        } catch (Exception e) {
            log.error("Lỗi khi restore từ file upload: {}", e.getMessage(), e);
            throw new IdInvalidException("Lỗi khi restore: " + e.getMessage());
        }
    }

    // 3. Restore backup từ file đã lưu trên MinIO
    @Transactional
    public BackupResponse restoreFromMinIO(String fileName, BackupType backupType) {
        log.info("Bắt đầu restore từ file MinIO: {}", fileName);

        BackupHistory backupHistory = backupHistoryRepository.findByFileName(fileName)
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy backup với tên: " + fileName));

        if (backupHistory.getStatus() != BackupStatus.SUCCESS) {
            throw new IdInvalidException("Backup này không thành công, không thể restore.");
        }

        try {
            InputStream backupStream = fileService.downloadFile(fileName);
            byte[] backupData = backupStream.readAllBytes();

            switch (backupType) {
                case DATABASE -> restoreDatabase(backupData);
                case FILES -> restoreMinioFiles(backupData);
                case FULL -> restoreFullBackup(backupData);
                default -> throw new IdInvalidException("Loại restore không hợp lệ: " + backupType);
            }

            log.info("Restore thành công từ file MinIO: {}", fileName);
            return toBackupResponse(backupHistory, "Restore thành công!");

        } catch (Exception e) {
            log.error("Lỗi khi restore: {}", e.getMessage(), e);
            throw new IdInvalidException("Lỗi khi restore: " + e.getMessage());
        }
    }

    // 3. Lấy lịch sử backup
    public List<BackupResponse> getBackupHistory() {
        List<BackupHistory> histories = backupHistoryRepository.findAllByOrderByCreatedAtDesc();
        return histories.stream().map(h -> toBackupResponse(h, null)).toList();
    }

    // 4. Lấy lịch sử backup theo loại
    public List<BackupResponse> getBackupHistoryByType(BackupType backupType) {
        List<BackupHistory> histories = backupHistoryRepository.findByBackupTypeOrderByCreatedAtDesc(backupType);
        return histories.stream().map(h -> toBackupResponse(h, null)).toList();
    }

    // 5. Xóa backup
    @Transactional
    public void deleteBackup(String backupId) {
        BackupHistory backupHistory = backupHistoryRepository.findById(backupId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy backup với id: " + backupId));

        try {
            if (backupHistory.getFileName() != null && fileService.fileExists(backupHistory.getFileName())) {
                fileService.deleteFile(backupHistory.getFileName());
            }
            backupHistoryRepository.delete(backupHistory);
            log.info("Đã xóa backup: {}", backupId);
        } catch (Exception e) {
            log.error("Lỗi khi xóa backup: {}", e.getMessage(), e);
            throw new IdInvalidException("Lỗi khi xóa backup: " + e.getMessage());
        }
    }

    // 6. Download backup (trả về InputStream)
    public InputStream downloadBackup(String fileName) throws Exception {
        if (!fileService.fileExists(fileName)) {
            throw new IdInvalidException("Không tìm thấy file backup: " + fileName);
        }
        return fileService.downloadFile(fileName);
    }

    // ==================== PRIVATE METHODS ====================

    private byte[] backupDatabase() throws Exception {
        log.info("Đang backup database...");

        String host = extractHostFromUrl(datasourceUrl);
        String port = extractPortFromUrl(datasourceUrl);
        String database = extractDatabaseFromUrl(datasourceUrl);

        List<String> command = new ArrayList<>();
        command.add("pg_dump");
        command.add("-h");
        command.add(host);
        command.add("-p");
        command.add(port);
        command.add("-U");
        command.add(datasourceUsername);
        command.add("-d");
        command.add(database);
        command.add("-F");
        command.add("p"); // Plain text format
        command.add("--clean"); // Thêm lệnh DROP trước CREATE để có thể restore đè lên dữ liệu cũ
        command.add("--if-exists"); // Không báo lỗi nếu object chưa tồn tại khi DROP

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("PGPASSWORD", datasourcePassword);
        processBuilder.redirectErrorStream(false);

        Process process = processBuilder.start();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = process.getInputStream()) {
            inputStream.transferTo(outputStream);
        }

        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("pg_dump failed with exit code " + exitCode + ": " + errorOutput);
        }

        log.info("Backup database thành công, kích thước: {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }

    private void restoreDatabase(byte[] backupData) throws Exception {
        log.info("Đang restore database... kích thước file: {} bytes", backupData.length);

        String host = extractHostFromUrl(datasourceUrl);
        String port = extractPortFromUrl(datasourceUrl);
        String database = extractDatabaseFromUrl(datasourceUrl);

        // Tạo file tạm để lưu backup data
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("backup_restore_", ".sql");
        java.nio.file.Files.write(tempFile, backupData);
        log.info("Đã tạo file tạm: {}", tempFile.toAbsolutePath());

        try {
            List<String> command = new ArrayList<>();
            command.add("psql");
            command.add("-h");
            command.add(host);
            command.add("-p");
            command.add(port);
            command.add("-U");
            command.add(datasourceUsername);
            command.add("-d");
            command.add(database);
            command.add("-f");
            command.add(tempFile.toAbsolutePath().toString());

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("PGPASSWORD", datasourcePassword);
            processBuilder.redirectErrorStream(true);

            log.info("Đang chạy lệnh psql...");
            Process process = processBuilder.start();

            // Đọc output trong thread riêng để tránh deadlock
            StringBuilder output = new StringBuilder();
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.error("Lỗi đọc output: {}", e.getMessage());
                }
            });
            outputReader.start();

            // Đợi process hoàn thành với timeout 5 phút
            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.MINUTES);
            outputReader.join(5000); // Chờ thread đọc output tối đa 5 giây

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("psql restore timeout sau 5 phút");
            }

            int exitCode = process.exitValue();

            // Log output dù thành công hay thất bại để debug
            if (output.length() > 0) {
                log.info("psql output: {}", output.toString().substring(0, Math.min(2000, output.length())));
            }

            if (exitCode != 0) {
                log.error("psql restore failed. Exit code: {}", exitCode);
                throw new RuntimeException("psql restore failed with exit code " + exitCode + ": " + output);
            }

            log.info("Restore database thành công");

        } finally {
            // Xóa file tạm
            java.nio.file.Files.deleteIfExists(tempFile);
            log.info("Đã xóa file tạm");
        }
    }

    private byte[] backupMinioFiles() throws Exception {
        log.info("Đang backup MinIO files...");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .recursive(true)
                            .build());

            for (Result<Item> result : objects) {
                Item item = result.get();
                String objectName = item.objectName();

                if (objectName.startsWith(BACKUP_FOLDER)) {
                    continue;
                }

                try (InputStream inputStream = fileService.downloadFile(objectName)) {
                    ZipEntry zipEntry = new ZipEntry(objectName);
                    zos.putNextEntry(zipEntry);
                    inputStream.transferTo(zos);
                    zos.closeEntry();
                }
            }
        }

        log.info("Backup MinIO files thành công, kích thước: {} bytes", baos.size());
        return baos.toByteArray();
    }

    private void restoreMinioFiles(byte[] backupData) throws Exception {
        log.info("Đang restore MinIO files...");

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(backupData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    byte[] fileContent = zis.readAllBytes();

                    String contentType = determineContentType(fileName);
                    fileService.uploadBytesWithName(fileContent, fileName, contentType);
                }
                zis.closeEntry();
            }
        }

        log.info("Restore MinIO files thành công");
    }

    private byte[] createFullBackup(String baseFileName) throws Exception {
        log.info("Đang tạo full backup...");

        byte[] dbBackup = backupDatabase();
        byte[] filesBackup = backupMinioFiles();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry dbEntry = new ZipEntry("database.sql");
            zos.putNextEntry(dbEntry);
            zos.write(dbBackup);
            zos.closeEntry();

            ZipEntry filesEntry = new ZipEntry("files.zip");
            zos.putNextEntry(filesEntry);
            zos.write(filesBackup);
            zos.closeEntry();
        }

        log.info("Full backup thành công, kích thước: {} bytes", baos.size());
        return baos.toByteArray();
    }

    private void restoreFullBackup(byte[] backupData) throws Exception {
        log.info("Đang restore full backup...");

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(backupData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                byte[] content = zis.readAllBytes();

                if ("database.sql".equals(entryName)) {
                    restoreDatabase(content);
                } else if ("files.zip".equals(entryName)) {
                    restoreMinioFiles(content);
                }
                zis.closeEntry();
            }
        }

        log.info("Full restore thành công");
    }

    private void sendBackupEmail(String toEmail, String fileName, byte[] backupData, BackupType backupType) {
        try {
            emailService.sendBackupEmail(toEmail, fileName, backupData, backupType);
            log.info("Đã gửi email backup đến: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email backup: {}", e.getMessage(), e);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String personnelId = jwt.getSubject();
            return userRepository.findByPersonnelId(personnelId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy user hiện tại"));
        }
        throw new IdInvalidException("Không thể xác định user hiện tại");
    }

    private BackupResponse toBackupResponse(BackupHistory history, String message) {
        String downloadUrl = null;
        if (history.getStatus() == BackupStatus.SUCCESS && history.getFileName() != null) {
            try {
                downloadUrl = fileService.getPresignedUrl(history.getFileName(), 3600);
            } catch (Exception e) {
                log.warn("Không thể tạo download URL: {}", e.getMessage());
            }
        }

        return new BackupResponse(
                history.getId(),
                history.getBackupType(),
                history.getFileName(),
                history.getFileSize(),
                history.getStatus(),
                message != null ? message : history.getErrorMessage(),
                history.getCreatedAt(),
                history.getCompletedAt(),
                downloadUrl,
                history.getCreatedByUser() != null ? history.getCreatedByUser().getFullName() : null);
    }

    private String extractHostFromUrl(String url) {
        String cleaned = url.replace("jdbc:postgresql://", "");
        String hostPort = cleaned.split("/")[0];
        return hostPort.split(":")[0];
    }

    private String extractPortFromUrl(String url) {
        String cleaned = url.replace("jdbc:postgresql://", "");
        String hostPort = cleaned.split("/")[0];
        String[] parts = hostPort.split(":");
        return parts.length > 1 ? parts[1] : "5432";
    }

    private String extractDatabaseFromUrl(String url) {
        String cleaned = url.replace("jdbc:postgresql://", "");
        String[] parts = cleaned.split("/");
        if (parts.length > 1) {
            return parts[1].split("\\?")[0];
        }
        return "postgres";
    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".pdf"))
            return "application/pdf";
        if (fileName.endsWith(".png"))
            return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return "image/jpeg";
        if (fileName.endsWith(".doc"))
            return "application/msword";
        if (fileName.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (fileName.endsWith(".xls"))
            return "application/vnd.ms-excel";
        if (fileName.endsWith(".xlsx"))
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return "application/octet-stream";
    }
}
