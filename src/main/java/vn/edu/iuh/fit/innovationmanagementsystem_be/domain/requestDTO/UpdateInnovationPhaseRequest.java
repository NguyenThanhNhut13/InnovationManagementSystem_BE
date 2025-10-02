package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInnovationPhaseRequest {

    private String name;
    private LocalDate roundStartDate;
    private LocalDate roundEndDate;
    private InnovationRoundStatusEnum status;

    // Thông tin giai đoạn cụ thể
    private InnovationPhaseTypeEnum phaseType;
    private LocalDate phaseStartDate;
    private LocalDate phaseEndDate;
    private String description;
    private Boolean isActive;
    private Integer phaseOrder;
    private Boolean isDeadline;
}
