package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "report_innovation_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportInnovationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "data_fields", columnDefinition = "JSON")
    private String dataFields;

    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_id")
    private Innovation innovation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Pre-persist and pre-update methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (createdBy == null) {
            createdBy = "system";
        }
        if (updatedBy == null) {
            updatedBy = "system";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (updatedBy == null) {
            updatedBy = "system";
        }
    }
}