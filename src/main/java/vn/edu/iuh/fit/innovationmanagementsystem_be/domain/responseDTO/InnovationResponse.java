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
    private String basisText;

    // User info
    private String userId;
    private String userFullName;
    private String userEmail;

    // Department info
    private String departmentId;
    private String departmentName;
    private String departmentCode;

    // Innovation Phase info
    private String innovationPhaseId;

    // Innovation Round info
    private String innovationRoundId;
    private String innovationRoundName;
    private String academicYear;

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Số ngày trễ khi nộp sáng kiến (null nếu không trễ)
    private Long lateSubmissionDays;
}
