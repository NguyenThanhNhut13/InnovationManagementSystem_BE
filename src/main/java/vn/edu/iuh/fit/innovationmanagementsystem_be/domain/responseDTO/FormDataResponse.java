package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDataResponse {

    // Basic FormData info
    private String id;
    private String fieldValue;
    private String formFieldId;
    private String formFieldLabel;
    private String formFieldKey;
    private FieldTypeEnum fieldType;
    private Boolean isRequired;
    private Integer orderInTemplate;
    private String innovationId;
    private String innovationName;

    // Template info
    private String templateId;

    // List responses
    private List<FormDataResponse> formDataList;
    private List<FormFieldResponse> formFields;

}
