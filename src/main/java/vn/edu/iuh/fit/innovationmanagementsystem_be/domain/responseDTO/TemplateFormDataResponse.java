package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFormDataResponse {

    private String templateId;
    private TemplateTypeEnum templateType;
    private List<TemplateFieldResponse> fields;
    // formData object for FE draft loading (key: fieldKey, value: field value)
    private Map<String, Object> formData;

}
