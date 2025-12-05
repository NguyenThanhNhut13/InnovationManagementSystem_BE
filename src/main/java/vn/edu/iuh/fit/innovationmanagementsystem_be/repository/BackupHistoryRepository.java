package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.BackupHistory;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackupHistoryRepository extends JpaRepository<BackupHistory, String> {

    List<BackupHistory> findAllByOrderByCreatedAtDesc();

    List<BackupHistory> findByBackupTypeOrderByCreatedAtDesc(BackupType backupType);

    Optional<BackupHistory> findByFileName(String fileName);
}
