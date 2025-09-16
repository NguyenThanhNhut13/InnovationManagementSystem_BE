package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRoundResponse {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private InnovationRoundStatusEnum status;
    private String description;
    private Boolean isActive;
    private String academicYear;
    private String innovationDecisionId;
    private String innovationDecisionTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
