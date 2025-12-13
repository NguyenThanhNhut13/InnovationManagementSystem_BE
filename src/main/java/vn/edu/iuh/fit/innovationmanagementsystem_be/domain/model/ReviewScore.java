package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ViolationTypeEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_scores")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScore extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    // Scoring details - JSON array of {criteriaId, selectedSubCriteriaId, score,
    // notes}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scoring_details", columnDefinition = "JSON")
    private JsonNode scoringDetails;

    @Column(name = "total_score")
    private Integer totalScore;

    // Decision
    @Column(name = "is_approved")
    private Boolean isApproved; // true = Thông qua, false = Không thông qua

    @Column(name = "requires_supplementary_documents")
    private Boolean requiresSupplementaryDocuments;

    @Column(name = "detailed_comments", columnDefinition = "TEXT")
    private String detailedComments;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // Violation reporting fields
    @Column(name = "has_violation", columnDefinition = "BOOLEAN")
    private Boolean hasViolation; // true = Báo cáo vi phạm

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", columnDefinition = "VARCHAR(50)")
    private ViolationTypeEnum violationType; // Loại vi phạm: DUPLICATE, FEASIBILITY, QUALITY

    @Column(name = "violation_reason", columnDefinition = "TEXT")
    private String violationReason; // Lý do vi phạm chi tiết

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer; // Người chấm điểm

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_id", nullable = false)
    private Innovation innovation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private InnovationDecision innovationDecision;
}