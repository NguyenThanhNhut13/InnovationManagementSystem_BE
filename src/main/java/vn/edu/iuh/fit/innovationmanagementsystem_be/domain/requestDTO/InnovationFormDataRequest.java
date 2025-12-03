package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationFormDataRequest {

    @NotBlank(message = "Innovation Name không được để trống")
    private String innovationName;

    // InnovationPhaseID là optional - mặc định lấy phase SUBMISSION của round
    // OPEN
    private String innovationPhaseId;

    @NotBlank(message = "Template ID không được để trống")
    private String templateId;

    private InnovationStatusEnum status = InnovationStatusEnum.DRAFT;

    private Boolean isScore = false;

    private String baseOn;

    // Dữ liệu form với fieldKey và fieldValue
    private Map<String, Object> formData;

    // Cho update operations
    private String innovationId;
}
