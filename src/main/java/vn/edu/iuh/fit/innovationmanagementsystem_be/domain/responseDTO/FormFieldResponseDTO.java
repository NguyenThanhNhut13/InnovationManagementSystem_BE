package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FieldTypeEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldResponseDTO {

    private UUID id;

    private String label;

    private FieldTypeEnum fieldType;

    private Boolean required;

    private Integer orderIndex;

    private String fieldKey;

    private UUID formTemplateId;

    private String formTemplateName;

    public FormFieldResponseDTO(vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField formField) {
        this.id = formField.getId();
        this.label = formField.getLabel();
        this.fieldType = formField.getFieldType();
        this.required = formField.getRequired();
        this.orderIndex = formField.getOrderIndex();
        this.fieldKey = formField.getFieldKey();

        if (formField.getFormTemplate() != null) {
            this.formTemplateId = formField.getFormTemplate().getId();
            this.formTemplateName = formField.getFormTemplate().getName();
        }
    }
}