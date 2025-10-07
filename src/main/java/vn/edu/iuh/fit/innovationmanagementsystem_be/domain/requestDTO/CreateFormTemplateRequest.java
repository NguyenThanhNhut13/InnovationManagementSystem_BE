package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFormTemplateRequest {

    @NotNull(message = "Loại template không được để trống")
    private TemplateTypeEnum templateType;

    private String templateContent;

    @NotNull(message = "ID của innovation phase không được để trống")
    private String innovationPhaseId;
}
