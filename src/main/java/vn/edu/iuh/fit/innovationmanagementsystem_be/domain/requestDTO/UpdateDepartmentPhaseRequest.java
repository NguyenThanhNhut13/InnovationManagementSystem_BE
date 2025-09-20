package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentPhaseRequest {
    private InnovationPhaseEnum phaseType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Boolean isActive;
    private Integer phaseOrder;
    private String innovationPhaseId;
}
