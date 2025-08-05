package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDataResponseDTO {

    private UUID id;

    private String value;

    private UUID formFieldId;

    private String formFieldLabel;

    private String formFieldType;

    private UUID innovationId;

    private String innovationName;

    public FormDataResponseDTO(vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData formData) {
        this.id = formData.getId();
        this.value = formData.getValue();

        if (formData.getFormField() != null) {
            this.formFieldId = formData.getFormField().getId();
            this.formFieldLabel = formData.getFormField().getLabel();
            this.formFieldType = formData.getFormField().getFieldType() != null
                    ? formData.getFormField().getFieldType().name()
                    : null;
        }

        if (formData.getInnovation() != null) {
            this.innovationId = formData.getInnovation().getId();
            this.innovationName = formData.getInnovation().getInnovationName();
        }
    }
}