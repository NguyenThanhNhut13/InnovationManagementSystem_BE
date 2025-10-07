package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFormTemplateRequest {

    private TemplateTypeEnum templateType;

    private TargetRoleCode targetRole;

    private String templateContent;
}
