package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "department_split_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentSplitHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "source_department_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String sourceDepartmentId;

    @Column(name = "source_department_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String sourceDepartmentName;

    @Column(name = "source_department_code", nullable = false, columnDefinition = "VARCHAR(50)")
    private String sourceDepartmentCode;

    @ElementCollection
    @CollectionTable(name = "department_split_new_ids", joinColumns = @JoinColumn(name = "split_history_id"))
    @Column(name = "new_department_id", columnDefinition = "VARCHAR(36)")
    private List<String> newDepartmentIds;

    @ElementCollection
    @CollectionTable(name = "department_split_new_names", joinColumns = @JoinColumn(name = "split_history_id"))
    @Column(name = "new_department_name", columnDefinition = "VARCHAR(255)")
    private List<String> newDepartmentNames;

    @ElementCollection
    @CollectionTable(name = "department_split_new_codes", joinColumns = @JoinColumn(name = "split_history_id"))
    @Column(name = "new_department_code", columnDefinition = "VARCHAR(50)")
    private List<String> newDepartmentCodes;

    @Column(name = "split_reason", columnDefinition = "TEXT")
    private String splitReason;

    @Column(name = "split_by", nullable = false, columnDefinition = "VARCHAR(255)")
    private String splitBy;

    @Column(name = "split_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime splitAt;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN")
    private Boolean isActive = true;

    @Column(name = "rollback_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime rollbackAt;

    @Column(name = "rollback_by", columnDefinition = "VARCHAR(255)")
    private String rollbackBy;

    @PrePersist
    protected void onCreate() {
        splitAt = LocalDateTime.now();
        isActive = true;
    }
}