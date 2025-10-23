package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

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

    // Template info
    private String formTemplateId;
    private JsonNode tableConfig;
    private JsonNode options;
    private Boolean repeatable;
    private JsonNode children;
    private JsonNode referenceConfig;
    private JsonNode userDataConfig;
    private JsonNode innovationDataConfig;

    private UserRoleEnum signingRole;

}