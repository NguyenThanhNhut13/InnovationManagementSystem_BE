package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewLevelEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilRequestDTO {

    private String name;

    private ReviewLevelEnum reviewCouncilLevel;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Council name is required")
    @jakarta.validation.constraints.Size(min = 3, max = 255, message = "Council name must be between 3 and 255 characters")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @jakarta.validation.constraints.NotNull(message = "Review council level is required")
    public ReviewLevelEnum getReviewCouncilLevel() {
        return reviewCouncilLevel;
    }

    public void setReviewCouncilLevel(ReviewLevelEnum reviewCouncilLevel) {
        this.reviewCouncilLevel = reviewCouncilLevel;
    }
}