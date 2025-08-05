package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDataRequestDTO {

    private String value;

    private UUID formFieldId;

    private UUID innovationId;

    // Validation annotations
    @jakarta.validation.constraints.NotNull(message = "Form field ID is required")
    public UUID getFormFieldId() {
        return formFieldId;
    }

    public void setFormFieldId(UUID formFieldId) {
        this.formFieldId = formFieldId;
    }

    @jakarta.validation.constraints.NotNull(message = "Innovation ID is required")
    public UUID getInnovationId() {
        return innovationId;
    }

    public void setInnovationId(UUID innovationId) {
        this.innovationId = innovationId;
    }
}