package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInnovationWithTemplatesRequest {

    @NotBlank(message = "Innovation Name không được để trống")
    private String innovationName;

    // Innovation Phase ID là optional - mặc định lấy phase SUBMISSION của round
    // OPEN
    private String innovationPhaseId;

    private InnovationStatusEnum status = InnovationStatusEnum.DRAFT;

    private Boolean isScore = false;

    private String basisText;

    // Innovation ID để update DRAFT sang SUBMITTED (optional)
    private String innovationId;

    @NotEmpty(message = "Danh sách template không được để trống")
    @Valid
    private List<TemplateDataRequest> templates;
}
