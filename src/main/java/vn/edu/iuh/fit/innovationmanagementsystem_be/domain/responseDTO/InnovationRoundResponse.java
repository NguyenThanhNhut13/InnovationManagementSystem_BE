package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationDecisionRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRoundResponse {
    private String id;
    private String name;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private InnovationRoundStatusEnum status;
    private String description;
    private String academicYear;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private InnovationDecisionResponse innovationDecision;
    private Set<InnovationPhaseResponse> innovationPhase;
}
