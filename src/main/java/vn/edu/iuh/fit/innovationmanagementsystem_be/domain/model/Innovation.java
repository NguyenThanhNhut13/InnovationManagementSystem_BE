package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "innovations")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Innovation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "innovation_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String innovationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50)")
    private InnovationStatusEnum status;

    @Column(name = "is_score", columnDefinition = "BOOLEAN")
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

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_decision_id")
    private InnovationDecision innovationDecision;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = Attachment.class)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CoInnovation> coInnovations = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormData> formDataList = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReviewScore> reviewScores = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReviewComment> reviewComments = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();

    @PrePersist
    protected void ensureDefaults() {
        if (status == null) {
            status = InnovationStatusEnum.DRAFT;
        }
    }
}