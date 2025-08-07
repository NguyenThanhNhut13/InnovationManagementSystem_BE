package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberRequestDTO {

    private UUID councilId;

    private UUID userId;

    // Validation annotations
    @jakarta.validation.constraints.NotNull(message = "Council ID is required")
    public UUID getCouncilId() {
        return councilId;
    }

    public void setCouncilId(UUID councilId) {
        this.councilId = councilId;
    }

    @jakarta.validation.constraints.NotNull(message = "User ID is required")
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}