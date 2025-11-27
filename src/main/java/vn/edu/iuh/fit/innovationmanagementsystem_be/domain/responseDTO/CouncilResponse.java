package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
