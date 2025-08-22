package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

@Getter
@Setter
public class InnovationRoundResponse {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private InnovationRoundStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String innovationDecisionId;
}
