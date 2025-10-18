package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InnovationStatisticsDTO {
    // Thống kê cơ bản
    private long totalInnovations; // Tổng số sáng kiến từ trước tới giờ
    private long submittedInnovations; // Sáng kiến đã nộp trong round này
    private long approvedInnovations; // Sáng kiến được duyệt (TRUONG_APPROVED)
    private long rejectedInnovations; // Sáng kiến bị trả lại (KHOA_REJECTED hoặc TRUONG_REJECTED)

    // Phần trăm
    private double achievedPercentage; // % đạt
    private double notAchievedPercentage; // % không đạt
    private double pendingPercentage; // % đang chờ duyệt
}
