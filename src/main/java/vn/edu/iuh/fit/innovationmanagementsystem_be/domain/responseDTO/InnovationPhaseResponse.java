package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationPhaseResponse {

    private String id;
    private String name;
    private InnovationPhaseTypeEnum phaseType;
    private LocalDate phaseStartDate;
    private LocalDate phaseEndDate;
    private String description;
    private InnovationPhaseLevelEnum level;
    private Integer phaseOrder;
    private Boolean isDeadline;
    private Boolean allowLateSubmission;

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
