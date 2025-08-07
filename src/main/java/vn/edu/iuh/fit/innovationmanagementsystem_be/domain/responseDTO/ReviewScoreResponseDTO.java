package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScoreResponseDTO {

    private UUID id;

    private String content;

    private String scoreLevel;

    private Integer actualScore;

    private UUID councilMemberId;
    private String councilMemberName;
    private String councilName;

    private UUID innovationId;
    private String innovationName;

    private UUID decisionId;
    private String decisionNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    // Constructor to convert from ReviewScore entity
    public ReviewScoreResponseDTO(ReviewScore reviewScore) {
        this.id = reviewScore.getId();
        this.content = reviewScore.getContent();
        this.scoreLevel = reviewScore.getScoreLevel();
        this.actualScore = reviewScore.getActualScore();
        this.createdAt = reviewScore.getCreatedAt();
        this.updatedAt = reviewScore.getUpdatedAt();
        this.createdBy = reviewScore.getCreatedBy();
        this.updatedBy = reviewScore.getUpdatedBy();

        // Set council member information if available
        if (reviewScore.getCouncilMember() != null) {
            this.councilMemberId = reviewScore.getCouncilMember().getId();
            if (reviewScore.getCouncilMember().getUser() != null) {
                this.councilMemberName = reviewScore.getCouncilMember().getUser().getFullName();
            }
            if (reviewScore.getCouncilMember().getCouncil() != null) {
                this.councilName = reviewScore.getCouncilMember().getCouncil().getName();
            }
        }

        // Set innovation information if available
        if (reviewScore.getInnovation() != null) {
            this.innovationId = reviewScore.getInnovation().getId();
            this.innovationName = reviewScore.getInnovation().getInnovationName();
        }

        // Set decision information if available
        if (reviewScore.getInnovationDecision() != null) {
            this.decisionId = reviewScore.getInnovationDecision().getId();
            this.decisionNumber = reviewScore.getInnovationDecision().getDecisionNumber();
        }
    }
}