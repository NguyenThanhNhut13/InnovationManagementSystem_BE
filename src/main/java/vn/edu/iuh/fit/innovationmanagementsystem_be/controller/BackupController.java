package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.BackupRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.BackupResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.BackupService;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system/backup")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_HE_THONG')")
public class BackupController {

    private final BackupService backupService;

    // 1. Tạo backup
    @PostMapping
    public ResponseEntity<BackupResponse> createBackup(@RequestBody BackupRequest request) {
        log.info("API: Tạo backup với loại: {}", request.backupType());
        BackupResponse response = backupService.createBackup(request);
        return ResponseEntity.ok(response);
    }

    // 2. Restore backup từ file upload
    @PostMapping(value = "/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BackupResponse> restoreBackup(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "backupType", required = false) BackupType backupType) {
        // Tự động detect backupType từ file extension nếu không truyền
        if (backupType == null) {
            String fileName = file.getOriginalFilename();
            if (fileName != null) {
                if (fileName.endsWith(".sql")) {
                    backupType = BackupType.DATABASE;
                } else if (fileName.contains("_full.zip")) {
                    backupType = BackupType.FULL;
                } else if (fileName.endsWith(".zip")) {
                    backupType = BackupType.FILES;
                } else {
                    backupType = BackupType.DATABASE; // default
                }
            } else {
                backupType = BackupType.DATABASE;
            }
        }
        log.info("API: Restore từ file upload: {}, loại: {}", file.getOriginalFilename(), backupType);
        BackupResponse response = backupService.restoreFromUpload(file, backupType);
        return ResponseEntity.ok(response);
    }

    // 3. Restore backup từ file đã lưu trên MinIO
    @PostMapping("/restore-from-history")
    public ResponseEntity<BackupResponse> restoreFromHistory(
            @RequestParam("fileName") String fileName,
            @RequestParam("backupType") BackupType backupType) {
        log.info("API: Restore từ file trên MinIO: {}", fileName);
        BackupResponse response = backupService.restoreFromMinIO(fileName, backupType);
        return ResponseEntity.ok(response);
    }

    // 4. Lấy lịch sử backup
    @GetMapping("/history")
    public ResponseEntity<List<BackupResponse>> getBackupHistory(
            @RequestParam(required = false) BackupType backupType) {
        log.info("API: Lấy lịch sử backup, loại: {}", backupType);
        List<BackupResponse> histories;
        if (backupType != null) {
            histories = backupService.getBackupHistoryByType(backupType);
        } else {
            histories = backupService.getBackupHistory();
        }
        return ResponseEntity.ok(histories);
    }

    // 5. Download file backup
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String fileName) throws Exception {
        log.info("API: Download backup file: {}", fileName);
        InputStream inputStream = backupService.downloadBackup(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + extractFileName(fileName) + "\"")
                .body(new InputStreamResource(inputStream));
    }

    // 6. Xóa backup
    @DeleteMapping("/{backupId}")
    public ResponseEntity<Void> deleteBackup(@PathVariable String backupId) {
        log.info("API: Xóa backup với id: {}", backupId);
        backupService.deleteBackup(backupId);
        return ResponseEntity.noContent().build();
    }

    private String extractFileName(String path) {
        if (path == null)
            return "backup";
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
