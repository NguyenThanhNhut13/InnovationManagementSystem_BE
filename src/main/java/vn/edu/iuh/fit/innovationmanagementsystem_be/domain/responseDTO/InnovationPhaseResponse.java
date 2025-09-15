package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationPhaseResponse {

    private String id;

    private String name;
    private LocalDate roundStartDate;
    private LocalDate roundEndDate;
    private InnovationRoundStatusEnum status;

    // Thông tin giai đoạn cụ thể
    private InnovationPhaseEnum phaseType;
    private LocalDate phaseStartDate;
    private LocalDate phaseEndDate;
    private String description;
    private Boolean isActive;
    private Integer phaseOrder; // Thứ tự giai đoạn (1, 2, 3, 4)

    // InnovationDecision info
    private String innovationDecisionId;
    private String innovationDecisionTitle;

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
