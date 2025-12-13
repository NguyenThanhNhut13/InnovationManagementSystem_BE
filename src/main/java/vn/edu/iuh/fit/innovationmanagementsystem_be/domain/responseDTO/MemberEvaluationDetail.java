package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberEvaluationDetail {
    private String memberId;
    private String memberName;
    private String memberRole; // CHU_TICH, THU_KY, THANH_VIEN
    private Integer totalScore; // null nếu innovation không chấm điểm
    private Boolean isApproved; // true = Thông qua, false = Không thông qua, null nếu chưa chấm
    private String comments;
    private LocalDateTime reviewedAt; // null nếu chưa chấm
    private Boolean hasScored; // true nếu đã chấm điểm
    // Violation fields
    private Boolean hasViolation; // true nếu thành viên này báo vi phạm
    private String violationType; // Loại vi phạm: DUPLICATE, FEASIBILITY, QUALITY
    private String violationReason; // Lý do vi phạm
}

