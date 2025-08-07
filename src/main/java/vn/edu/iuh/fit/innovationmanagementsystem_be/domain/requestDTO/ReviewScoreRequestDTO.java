package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScoreRequestDTO {

    private String content;

    private String scoreLevel;

    private Integer actualScore;

    private UUID councilMemberId;

    private UUID innovationId;

    private UUID decisionId;

    private String createdBy;

    private String updatedBy;

    // Validation annotations
    @jakarta.validation.constraints.NotNull(message = "Council member ID is required")
    public UUID getCouncilMemberId() {
        return councilMemberId;
    }

    public void setCouncilMemberId(UUID councilMemberId) {
        this.councilMemberId = councilMemberId;
    }

    @jakarta.validation.constraints.NotNull(message = "Innovation ID is required")
    public UUID getInnovationId() {
        return innovationId;
    }

    public void setInnovationId(UUID innovationId) {
        this.innovationId = innovationId;
    }

    @jakarta.validation.constraints.Min(value = 0, message = "Actual score must be greater than or equal to 0")
    @jakarta.validation.constraints.Max(value = 100, message = "Actual score must be less than or equal to 100")
    public Integer getActualScore() {
        return actualScore;
    }

    public void setActualScore(Integer actualScore) {
        this.actualScore = actualScore;
    }
}