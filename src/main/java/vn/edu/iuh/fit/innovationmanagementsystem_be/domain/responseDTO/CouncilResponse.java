package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ScoringPeriodStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilResponse {

    private String id;
    private String name;
    private ReviewLevelEnum reviewCouncilLevel;
    private CouncilStatusEnum status; // Trạng thái hội đồng
    private String departmentName; // null nếu cấp trường
    private String roundName; // Tên đợt sáng kiến
    private List<CouncilMemberResponse> members;
    private Integer innovationCount;
    private ScoringProgressResponse scoringProgress; // Thông tin tiến độ chấm điểm
    
    // Scoring period information (chung cho tất cả innovations trong council)
    private LocalDate scoringStartDate;
    private LocalDate scoringEndDate;
    private Boolean canScore; // true nếu đang trong thời gian chấm điểm
    private Boolean canView; // true nếu có thể xem (trong thời gian chấm điểm hoặc xem trước 3 ngày)
    private ScoringPeriodStatusEnum scoringPeriodStatus; // Trạng thái thời gian chấm điểm
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
