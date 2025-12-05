package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupStatus;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.BackupType;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BackupHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_type", nullable = false)
    private BackupType backupType;

    @Column(name = "file_name", nullable = false, columnDefinition = "VARCHAR(500)")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BackupStatus status = BackupStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = BackupStatus.PENDING;
        }
    }
}
