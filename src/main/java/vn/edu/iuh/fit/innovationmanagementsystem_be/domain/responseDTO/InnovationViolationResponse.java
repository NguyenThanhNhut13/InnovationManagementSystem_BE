package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationViolationResponse {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String departmentName;
    private Boolean isScore;
    private List<ViolationDetail> violations; // Danh sách các vi phạm được báo cáo
    private Integer violationCount; // Số lượng vi phạm
    private Boolean needsChairmanReview; // true nếu cần Chủ tịch xem xét (chưa được xử lý)
}

