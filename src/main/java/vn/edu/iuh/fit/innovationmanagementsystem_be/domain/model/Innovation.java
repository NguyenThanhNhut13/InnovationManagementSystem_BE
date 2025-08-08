package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "innovations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Innovation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "innovation_name", nullable = false)
    private String innovationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InnovationStatusEnum status;

    @Column(name = "is_score")
    private Boolean isScore;

    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_round_id", nullable = false)
    private InnovationRound innovationRound;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Relationships
    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = Attachment.class)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CoInnovation> coInnovations = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormData> formDataList = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewScore> reviewScores;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewComment> reviewComments;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReportInnovationDetail> reportInnovationDetails;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DigitalSignature> digitalSignatures;

    // Pre-persist and pre-update methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InnovationStatusEnum.DRAFT;
        }
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