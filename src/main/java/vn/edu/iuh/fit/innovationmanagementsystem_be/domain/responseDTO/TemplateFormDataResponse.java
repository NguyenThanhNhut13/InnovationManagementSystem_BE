package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import java.util.List;
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

}
