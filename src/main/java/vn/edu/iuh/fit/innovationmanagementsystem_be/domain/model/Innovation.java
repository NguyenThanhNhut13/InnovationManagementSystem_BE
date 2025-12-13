package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50)")
    private InnovationStatusEnum status;

    @Column(name = "is_score", columnDefinition = "BOOLEAN")
    private Boolean isScore;

    @Column(name = "basis_text", columnDefinition = "TEXT")
    private String basisText;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "embedding", columnDefinition = "vector(384)", insertable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private String embedding;

    // Relationships

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormData> formDataList = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, targetEntity = Attachment.class)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CoInnovation> coInnovations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReviewScore> reviewScores = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_round_id")
    private InnovationRound innovationRound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_phase_id")
    private InnovationPhase innovationPhase;

    @ManyToMany(mappedBy = "innovations", fetch = FetchType.LAZY)
    private List<Council> councils = new ArrayList<>();

    @PrePersist
    protected void ensureDefaults() {
        if (status == null) {
            status = InnovationStatusEnum.DRAFT;
        }
    }
}