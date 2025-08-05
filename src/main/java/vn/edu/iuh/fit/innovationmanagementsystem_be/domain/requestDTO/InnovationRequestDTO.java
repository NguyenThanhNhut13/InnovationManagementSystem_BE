package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRequestDTO {

    private String innovationName;

    private UUID userId;

    private UUID departmentId;

    private UUID innovationRoundId;

    private Boolean isScore;

    private Innovation.InnovationStatus status;

    private String createdBy;

    private String updatedBy;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Innovation name is required")
    @jakarta.validation.constraints.Size(min = 3, max = 255, message = "Innovation name must be between 3 and 255 characters")
    public String getInnovationName() {
        return innovationName;
    }

    public void setInnovationName(String innovationName) {
        this.innovationName = innovationName;
    }
}