package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationResultDetail {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String departmentName;
    private Boolean isScore; // true nếu sáng kiến có chấm điểm
    private Integer totalMembers; // Tổng số thành viên có quyền chấm điểm
    private Integer scoredMembers; // Số thành viên đã chấm điểm
    private Integer approvedCount; // Số thành viên thông qua
    private Integer rejectedCount; // Số thành viên không thông qua
    private Integer pendingCount; // Số thành viên chưa chấm
    private Double averageScore; // Điểm trung bình (null nếu không chấm điểm hoặc chưa có điểm)
    private Boolean finalDecision; // true = Thông qua, false = Không thông qua, null nếu chưa quyết định được hoặc có vi phạm cần Chủ tịch xem xét
    private String decisionReason; // Lý do quyết định (ví dụ: "Đa số thông qua", "Đa số không thông qua", "Bằng nhau - dựa vào điểm trung bình", "Bằng nhau - quyết định của Chủ tịch", "Có vi phạm - cần Chủ tịch xem xét")
    private List<MemberEvaluationDetail> memberEvaluations; // Chi tiết đánh giá của từng thành viên
    // Violation fields
    private Boolean hasViolation; // true nếu có bất kỳ thành viên nào báo vi phạm
    private List<ViolationDetail> violations; // Danh sách các vi phạm được báo cáo
}

