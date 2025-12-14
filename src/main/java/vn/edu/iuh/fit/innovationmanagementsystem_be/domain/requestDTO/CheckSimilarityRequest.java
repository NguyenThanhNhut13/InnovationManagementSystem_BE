package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckSimilarityRequest {
    private String innovationName;
    private List<TemplateFormData> templates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateFormData {
        private String templateId;
        private Map<String, Object> formData;
    }
}
