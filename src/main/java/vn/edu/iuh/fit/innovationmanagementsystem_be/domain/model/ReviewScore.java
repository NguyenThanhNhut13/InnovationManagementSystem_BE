package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScore extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "content", columnDefinition = "JSON")
    private String content;

    @Column(name = "score_level")
    private String scoreLevel;

    @Column(name = "actual_score")
    private Integer actualScore;

    // Foreign key relationships
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