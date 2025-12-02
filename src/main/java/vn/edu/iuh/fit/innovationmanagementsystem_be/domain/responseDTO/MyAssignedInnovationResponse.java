package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDate;

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
    private Boolean canScore;             // true nếu đang trong thời gian chấm điểm
    private Boolean canView;              // true nếu có thể xem (trong thời gian chấm điểm hoặc xem trước 3 ngày)
    private String scoringPeriodStatus;  // "NOT_STARTED" | "ACTIVE" | "ENDED" | "PREVIEW"
    private LocalDate scoringStartDate;   // Ngày bắt đầu chấm điểm
}

