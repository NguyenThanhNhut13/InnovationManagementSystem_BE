package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateWithContributionResponse {

    private String id;
    private TemplateTypeEnum templateType;
    private TargetRoleCode targetRole;
    private String templateContent;
    private String innovationRoundId;
    private String innovationRoundName;
    private List<FormFieldResponse> formFields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Thông tin contribution từ các template khác
    private List<ContributionData> contributionData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContributionData {
        private String sourceTemplateId;
        private String sourceTemplateType;
        private String sourceFieldId;
        private String sourceFieldKey;
        private String sourceFieldLabel;
        private JsonNode contributionConfig;
    }
}
