package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPhaseListResponse {
    private String id;
    private String name;
    private InnovationPhaseTypeEnum phaseType;
    private LocalDate phaseStartDate;
    private LocalDate phaseEndDate;
    private PhaseStatusEnum phaseStatus;
    private InnovationRoundStatusEnum status;
    private String innovationPhaseName;
    private String departmentName;
    private String academicYear;
}
