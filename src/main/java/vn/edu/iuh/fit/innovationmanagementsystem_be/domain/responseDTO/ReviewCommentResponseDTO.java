package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewComment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewLevelEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentResponseDTO {

    private UUID id;

    private String comment;

    private ReviewLevelEnum reviewsLevel;

    private UUID innovationId;
    private String innovationName;

    private UUID councilMemberId;
    private String councilMemberName;
    private String councilName;

    // Constructor to convert from ReviewComment entity
    public ReviewCommentResponseDTO(ReviewComment reviewComment) {
        this.id = reviewComment.getId();
        this.comment = reviewComment.getComment();
        this.reviewsLevel = reviewComment.getReviewsLevel();

        // Set innovation information if available
        if (reviewComment.getInnovation() != null) {
            this.innovationId = reviewComment.getInnovation().getId();
            this.innovationName = reviewComment.getInnovation().getInnovationName();
        }

        // Set council member information if available
        if (reviewComment.getCouncilMember() != null) {
            this.councilMemberId = reviewComment.getCouncilMember().getId();
            if (reviewComment.getCouncilMember().getUser() != null) {
                this.councilMemberName = reviewComment.getCouncilMember().getUser().getFullName();
            }
            if (reviewComment.getCouncilMember().getCouncil() != null) {
                this.councilName = reviewComment.getCouncilMember().getCouncil().getName();
            }
        }
    }
}