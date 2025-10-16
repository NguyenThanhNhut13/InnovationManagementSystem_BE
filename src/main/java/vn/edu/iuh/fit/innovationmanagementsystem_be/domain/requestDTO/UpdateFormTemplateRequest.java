package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
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

    @Valid
    private java.util.List<FieldData> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldData {
        private String id; // nếu null => tạo mới
        private String fieldKey;
        private String label;
        private vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum type;
        private Boolean required;
        private String placeholder;
        private CreateTemplateWithFieldsRequest.TableConfigData tableConfig;
        private java.util.List<String> options;
        private Boolean repeatable;
        @Valid
        private java.util.List<CreateTemplateWithFieldsRequest.FieldData> children;

    }
}
