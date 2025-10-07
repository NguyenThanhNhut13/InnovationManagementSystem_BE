package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldResponse {

    // Basic field info
    private String id;
    private String fieldKey;
    private String label;
    private FieldTypeEnum fieldType;
    private Boolean required;
    private String placeholder;

    // Template info
    private String formTemplateId;

    // Table config for TABLE field type
    private JsonNode tableConfig;

}