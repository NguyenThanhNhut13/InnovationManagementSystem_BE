package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentPhaseStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private InnovationRoundStatusEnum status;
}
