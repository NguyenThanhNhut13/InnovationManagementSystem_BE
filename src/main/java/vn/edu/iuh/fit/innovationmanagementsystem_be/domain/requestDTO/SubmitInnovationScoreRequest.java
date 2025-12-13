package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitInnovationScoreRequest {

    // Optional - chỉ bắt buộc nếu isScore = true
    @Valid
    private List<ScoreCriteriaDetail> scoringDetails;

    // Optional - chỉ bắt buộc nếu isScore = true
    @Min(value = 0, message = "Tổng điểm phải >= 0")
    // Note: Max validation được thực hiện trong service dựa trên maxTotalScore từ scoring criteria
    private Integer totalScore;

    @NotNull(message = "Quyết định đánh giá không được để trống")
    private Boolean isApproved; // true = Thông qua, false = Không thông qua

    private Boolean requiresSupplementaryDocuments; // Yêu cầu bổ sung tài liệu

    // Optional - có thể để trống
    @Size(max = 5000, message = "Nhận xét không được vượt quá 5000 ký tự")
    private String detailedComments;

    // Violation reporting fields
    private Boolean hasViolation; // true = Báo cáo vi phạm

    private String violationType; // Loại vi phạm: DUPLICATE, FEASIBILITY, QUALITY

    @Size(max = 2000, message = "Lý do vi phạm không được vượt quá 2000 ký tự")
    private String violationReason; // Lý do vi phạm chi tiết
}
