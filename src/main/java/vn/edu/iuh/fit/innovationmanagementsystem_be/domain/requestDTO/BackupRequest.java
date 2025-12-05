package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType;

public record BackupRequest(
        BackupType backupType,
        boolean sendEmail,
        String description,
        String email) {
    public BackupRequest {
        if (backupType == null) {
            backupType = BackupType.DATABASE;
        }
    }
}
