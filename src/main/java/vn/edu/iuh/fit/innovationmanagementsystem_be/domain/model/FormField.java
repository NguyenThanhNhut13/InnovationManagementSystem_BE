package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "form_field")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "label", nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type")
    private FieldTypeEnum fieldType;

    @Column(name = "required")
    private Boolean required;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "field_key")
    private String fieldKey;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_template_id")
    private FormTemplate formTemplate;

    @OneToMany(mappedBy = "formField", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FormData> formDataList;

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