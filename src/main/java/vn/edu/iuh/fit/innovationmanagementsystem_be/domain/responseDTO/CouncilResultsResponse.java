package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilResultsResponse {
    private String councilId;
    private String councilName;
    private String reviewCouncilLevel; // KHOA hoặc TRUONG
    private LocalDate scoringEndDate; // Ngày kết thúc chấm điểm
    private Boolean canViewResults; // true nếu đã hết thời gian chấm điểm
    private Integer totalInnovations; // Tổng số sáng kiến
    private Integer completedInnovations; // Số sáng kiến đã có đủ đánh giá từ tất cả thành viên
    private Integer pendingInnovations; // Số sáng kiến còn thiếu đánh giá
    private String warningMessage; // Cảnh báo nếu có (ví dụ: "Một số thành viên chưa chấm điểm")
    private List<InnovationResultDetail> innovationResults; // Kết quả chi tiết từng sáng kiến
}

