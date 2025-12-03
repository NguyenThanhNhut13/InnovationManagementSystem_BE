package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringProgressResponse {
    private Integer totalInnovations;        // Tổng số sáng kiến
    private Integer scoredCount;             // Số sáng kiến đã có đủ điểm từ tất cả thành viên
    private Integer pendingCount;            // Số sáng kiến chưa chấm đủ
    private Double averageScore;              // Điểm trung bình (null nếu chưa có điểm nào)
    private Integer completionPercentage;   // % hoàn thành (0-100)
}

