package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateWithFieldsRequest {

    @NotBlank(message = "Template content không được để trống")
    private String templateContent;

    @NotNull(message = "Template type không được để trống")
    private TemplateTypeEnum templateType;

    @NotNull(message = "Target role không được để trống")
    private TargetRoleCode targetRole;

    @NotBlank(message = "Round ID không được để trống")
    private String roundId;

    @NotEmpty(message = "Danh sách fields không được để trống")
    @Valid
    private List<FieldData> fields;
}
