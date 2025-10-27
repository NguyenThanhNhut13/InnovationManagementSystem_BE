package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDataResponse {

    private String id;
    private String formFieldId;
    private String formFieldLabel;
    private JsonNode fieldValue;
    private String formFieldKey;
    private FieldTypeEnum fieldType;
    private Boolean required;
    private String templateId;

}
