package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupStatus;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType;

import java.time.LocalDateTime;

public record BackupResponse(
        String backupId,
        BackupType backupType,
        String fileName,
        Long fileSize,
        BackupStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        String downloadUrl,
        String createdByName) {
}
