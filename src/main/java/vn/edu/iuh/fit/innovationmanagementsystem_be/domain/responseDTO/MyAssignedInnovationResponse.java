package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAssignedInnovationResponse {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String departmentName;
    private InnovationStatusEnum status;
    
    // Fields cho thành viên hội đồng
    private Boolean isScore;              // true = có chấm điểm, false = không chấm điểm
    private Boolean myIsApproved;          // Quyết định của current user (null nếu chưa đánh giá)
    private LocalDateTime submittedAt;     // Thời gian nộp sáng kiến (để sort)
    // Note: Scoring period info (canScore, canView, scoringPeriodStatus, scoringStartDate) 
    // đã được di chuyển sang CouncilResponse để tránh lặp lại cho mỗi innovation
}

