package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "department_merge_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentMergeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "merged_department_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String mergedDepartmentId;

    @Column(name = "merged_department_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String mergedDepartmentName;

    @Column(name = "merged_department_code", nullable = false, columnDefinition = "VARCHAR(50)")
    private String mergedDepartmentCode;

    @ElementCollection
    @CollectionTable(name = "department_merge_source_ids", joinColumns = @JoinColumn(name = "merge_history_id"))
    @Column(name = "source_department_id", columnDefinition = "VARCHAR(36)")
    private List<String> sourceDepartmentIds;

    @Column(name = "merge_reason", columnDefinition = "TEXT")
    private String mergeReason;

    @Column(name = "merged_by", nullable = false, columnDefinition = "VARCHAR(255)")
    private String mergedBy;

    @Column(name = "merged_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime mergedAt;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN")
    private Boolean isActive = true;

    @Column(name = "rollback_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime rollbackAt;

    @Column(name = "rollback_by", columnDefinition = "VARCHAR(255)")
    private String rollbackBy;

    @PrePersist
    protected void onCreate() {
        mergedAt = LocalDateTime.now();
        isActive = true;
    }
}