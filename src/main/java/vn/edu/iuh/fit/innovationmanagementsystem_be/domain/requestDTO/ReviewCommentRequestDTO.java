package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewLevelEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentRequestDTO {

    private String comment;

    private ReviewLevelEnum reviewsLevel;

    private UUID innovationId;

    private UUID councilMemberId;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Comment is required")
    @jakarta.validation.constraints.Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @jakarta.validation.constraints.NotNull(message = "Review level is required")
    public ReviewLevelEnum getReviewsLevel() {
        return reviewsLevel;
    }

    public void setReviewsLevel(ReviewLevelEnum reviewsLevel) {
        this.reviewsLevel = reviewsLevel;
    }

    @jakarta.validation.constraints.NotNull(message = "Innovation ID is required")
    public UUID getInnovationId() {
        return innovationId;
    }

    public void setInnovationId(UUID innovationId) {
        this.innovationId = innovationId;
    }

    @jakarta.validation.constraints.NotNull(message = "Council member ID is required")
    public UUID getCouncilMemberId() {
        return councilMemberId;
    }

    public void setCouncilMemberId(UUID councilMemberId) {
        this.councilMemberId = councilMemberId;
    }
}