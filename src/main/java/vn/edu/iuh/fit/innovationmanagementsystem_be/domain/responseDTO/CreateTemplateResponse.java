package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateResponse {

    private String id;
    private String templateContent;
    private TemplateTypeEnum templateType;
    private TargetRoleCode targetRole;
    private String roundId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<FieldResponse> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldResponse {
        private String id;
        private String fieldKey;
        private String label;
        private FieldTypeEnum type;
        private Boolean required;
        private Object tableConfig;
        private Object options;
        private Boolean repeatable;
        private Object children;
        private Object referenceConfig;
        private Object userDataConfig;
        private Object innovationDataConfig;

        private Object contributionConfig;

        private UserRoleEnum signingRole;
    }
}
