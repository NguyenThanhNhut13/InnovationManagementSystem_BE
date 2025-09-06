package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationResponse {

    private String id;
    private String innovationName;
    private InnovationStatusEnum status;
    private Boolean isScore;

    // User info
    private String userId;
    private String userFullName;
    private String userEmail;

    // Department info
    private String departmentId;
    private String departmentName;
    private String departmentCode;

    // Innovation Round info
    private String innovationRoundId;
    private String innovationDecisionId;

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
