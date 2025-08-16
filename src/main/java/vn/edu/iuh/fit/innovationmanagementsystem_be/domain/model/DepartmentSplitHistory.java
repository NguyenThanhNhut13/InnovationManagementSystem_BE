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
    private String id;

    @Column(name = "source_department_id", nullable = false)
    private String sourceDepartmentId;

    @Column(name = "source_department_name", nullable = false)
    private String sourceDepartmentName;

    @Column(name = "source_department_code", nullable = false)
    private String sourceDepartmentCode;

    @ElementCollection
    @CollectionTable(name = "department_split_new_ids", joinColumns = @JoinColumn(name = "split_history_id"))
    @Column(name = "new_department_id")
    private List<String> newDepartmentIds;

    @ElementCollection
    @CollectionTable(name = "department_split_new_names", joinColumns = @JoinColumn(name = "split_history_id"))
    @Column(name = "new_department_name")
    private List<String> newDepartmentNames;

    @ElementCollection
    @CollectionTable(name = "department_split_new_codes", joinColumns = @JoinColumn(name = "split_history_id"))
    @Column(name = "new_department_code")
    private List<String> newDepartmentCodes;

    @Column(name = "split_reason")
    private String splitReason;

    @Column(name = "split_by", nullable = false)
    private String splitBy;

    @Column(name = "split_at", nullable = false)
    private LocalDateTime splitAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "rollback_at")
    private LocalDateTime rollbackAt;

    @Column(name = "rollback_by")
    private String rollbackBy;

    @PrePersist
    protected void onCreate() {
        splitAt = LocalDateTime.now();
        isActive = true;
    }
}