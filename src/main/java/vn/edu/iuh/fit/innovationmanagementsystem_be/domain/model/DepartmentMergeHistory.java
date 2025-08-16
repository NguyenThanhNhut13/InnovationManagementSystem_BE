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
    private String id;

    @Column(name = "merged_department_id", nullable = false)
    private String mergedDepartmentId;

    @Column(name = "merged_department_name", nullable = false)
    private String mergedDepartmentName;

    @Column(name = "merged_department_code", nullable = false)
    private String mergedDepartmentCode;

    @ElementCollection
    @CollectionTable(name = "department_merge_source_ids", joinColumns = @JoinColumn(name = "merge_history_id"))
    @Column(name = "source_department_id")
    private List<String> sourceDepartmentIds;

    @Column(name = "merge_reason")
    private String mergeReason;

    @Column(name = "merged_by", nullable = false)
    private String mergedBy;

    @Column(name = "merged_at", nullable = false)
    private LocalDateTime mergedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "rollback_at")
    private LocalDateTime rollbackAt;

    @Column(name = "rollback_by")
    private String rollbackBy;

    @PrePersist
    protected void onCreate() {
        mergedAt = LocalDateTime.now();
        isActive = true;
    }
}