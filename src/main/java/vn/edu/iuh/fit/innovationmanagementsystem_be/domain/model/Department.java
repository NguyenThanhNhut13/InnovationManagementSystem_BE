package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "department_code", nullable = false, unique = true)
    private String departmentCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "deletion_reason")
    private String deletionReason;

    // Relationships
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Innovation> innovations = new ArrayList<>();

    // Soft delete method
    public void softDelete(String deletedBy, String reason) {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.deletionReason = reason;
    }

    // Restore method
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
        this.deletedBy = null;
        this.deletionReason = null;
    }
}