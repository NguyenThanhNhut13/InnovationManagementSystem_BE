package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "review_scores")
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScore extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "JSON")
    private String content;

    @Column(name = "score_level")
    private String scoreLevel;

    @Column(name = "actual_score")
    private Integer actualScore;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "council_members_id", nullable = false)
    private CouncilMember councilMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_id", nullable = false)
    private Innovation innovation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private InnovationDecision innovationDecision;
}