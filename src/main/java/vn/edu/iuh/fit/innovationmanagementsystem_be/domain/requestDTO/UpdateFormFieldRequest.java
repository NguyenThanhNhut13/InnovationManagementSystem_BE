package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFormFieldRequest {

    private String id;

    private String label;

    private String fieldKey;

    private FieldTypeEnum fieldType;

    private Boolean required;

    private Boolean repeatable;

    private JsonNode userDataConfig;

    private JsonNode innovationDataConfig;

    private UserRoleEnum signingRole;

}
