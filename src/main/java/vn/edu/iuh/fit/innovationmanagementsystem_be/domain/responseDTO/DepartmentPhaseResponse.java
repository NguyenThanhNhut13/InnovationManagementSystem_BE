package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPhaseResponse {

    private String id;
    private String name;
    private InnovationPhaseTypeEnum phaseType;
    private Integer phaseOrder;
    private LocalDate phaseStartDate;
    private LocalDate phaseEndDate;
    private String description;
    private PhaseStatusEnum phaseStatus;
    private String innovationPhaseId;
    private String innovationPhaseName;
    private Boolean isDeadline;
    private String departmentId;
    private String departmentName;
    private String academicYear;

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
