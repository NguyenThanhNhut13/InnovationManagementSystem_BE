package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationWithScoreResponse {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String departmentName;
    private InnovationStatusEnum status;
    private Integer totalReviewers;        // Tổng số thành viên cần chấm (chỉ THANH_VIEN)
    private Integer scoredReviewers;       // Số thành viên đã chấm điểm
    private Double averageScore;           // Điểm trung bình (null nếu chưa đủ điểm)
    private Boolean isCompleted;           // true nếu đã có đủ điểm từ tất cả reviewers
}

