package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FieldTypeEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldRequestDTO {

    private String label;

    private FieldTypeEnum fieldType;

    private Boolean required;

    private Integer orderIndex;

    private String fieldKey;

    private UUID formTemplateId;

    // Validation annotations
    @jakarta.validation.constraints.NotBlank(message = "Field label is required")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @jakarta.validation.constraints.NotNull(message = "Field type is required")
    public FieldTypeEnum getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldTypeEnum fieldType) {
        this.fieldType = fieldType;
    }

    @jakarta.validation.constraints.NotNull(message = "Form template ID is required")
    public UUID getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(UUID formTemplateId) {
        this.formTemplateId = formTemplateId;
    }
}