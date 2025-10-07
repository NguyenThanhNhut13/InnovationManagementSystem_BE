package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMultipleFormTemplatesRequest {

    @NotNull(message = "ID của innovation phase không được để trống")
    private String innovationPhaseId;

    @NotEmpty(message = "Danh sách form templates không được để trống")
    @Valid
    private List<FormTemplateData> formTemplates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormTemplateData {

        @NotNull(message = "Loại template không được để trống")
        private TemplateTypeEnum templateType;

        private String templateContent;
    }
}
