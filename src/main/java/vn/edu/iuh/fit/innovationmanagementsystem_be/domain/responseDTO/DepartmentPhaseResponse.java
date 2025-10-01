package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPhaseResponse {

    private String id;
    private InnovationPhaseTypeEnum phaseType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Boolean isActive;
    private Integer phaseOrder;

    // Department info
    private String departmentId;
    private String departmentName;

    // InnovationPhase info
    private String innovationPhaseId;
    private String innovationPhaseName;

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
