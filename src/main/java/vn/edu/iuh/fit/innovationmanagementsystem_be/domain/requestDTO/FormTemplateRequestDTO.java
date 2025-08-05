package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateRequestDTO {

    private String name;

    private String description;

    private String templateContent;

    private String createdBy;

    private String updatedBy;

    private UUID innovationRoundId;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Template name is required")
    @jakarta.validation.constraints.Size(min = 3, max = 255, message = "Template name must be between 3 and 255 characters")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @jakarta.validation.constraints.Size(max = 1000, message = "Description must not exceed 1000 characters")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}