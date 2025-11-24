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

    @NotNull(message = "Danh sách điểm không được để trống")
    @Size(min = 1, message = "Phải chấm điểm cho ít nhất 1 tiêu chí")
    @Valid
    private List<ScoreCriteriaDetail> scoringDetails;

    @NotNull(message = "Tổng điểm không được để trống")
    @Min(value = 0, message = "Tổng điểm phải >= 0")
    @Max(value = 100, message = "Tổng điểm phải <= 100")
    private Integer totalScore;

    @NotNull(message = "Quyết định đánh giá không được để trống")
    private Boolean isApproved; // true = Thông qua, false = Không thông qua

    private Boolean requiresSupplementaryDocuments; // Yêu cầu bổ sung tài liệu

    @NotBlank(message = "Nhận xét chi tiết không được để trống")
    @Size(max = 5000, message = "Nhận xét không được vượt quá 5000 ký tự")
    private String detailedComments;
}
