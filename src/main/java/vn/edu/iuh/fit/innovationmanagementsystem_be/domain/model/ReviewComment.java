package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

@Entity
@Table(name = "review_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewComment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviews_level", nullable = false)
    private ReviewLevelEnum reviewsLevel;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_id", nullable = false)
    private Innovation innovation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_council_members_id", nullable = false)
    private CouncilMember councilMember;
}