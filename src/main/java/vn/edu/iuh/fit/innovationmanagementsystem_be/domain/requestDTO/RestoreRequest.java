package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType;

public record RestoreRequest(
        String backupFileName,
        BackupType backupType) {
}
