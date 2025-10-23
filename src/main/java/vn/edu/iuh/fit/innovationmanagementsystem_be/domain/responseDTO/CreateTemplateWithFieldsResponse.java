package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
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
public class CreateTemplateWithFieldsResponse {

    private String id;
    private String templateContent;
    private TemplateTypeEnum templateType;
    private TargetRoleCode targetRole;
    private String roundId;
    private List<FieldResponse> fields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldResponse {

        private String id;
        private String fieldKey;
        private String label;
        private FieldTypeEnum type;
        private Boolean required;
        private JsonNode tableConfig;
        private JsonNode options;
        private Boolean repeatable;
        private JsonNode children;
        private JsonNode referenceConfig;
        private JsonNode userDataConfig;
        private JsonNode innovationDataConfig;

        private UserRoleEnum signingRole;
    }
}
