package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ScoringPeriodStatusEnum;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class InnovationScoringDetailResponse extends DepartmentInnovationDetailResponse {
    
    private JsonNode scoringCriteria;  // Bảng điểm từ InnovationDecision
    private Integer maxTotalScore;      // Tổng điểm tối đa (thường = 100)
    
    // Scoring period information
    private LocalDate scoringStartDate;  // Ngày bắt đầu chấm điểm
    private LocalDate scoringEndDate;    // Ngày kết thúc chấm điểm
    private Boolean canScore;            // true nếu đang trong thời gian chấm điểm
    private Boolean canView;             // true nếu có thể xem (trong thời gian chấm điểm hoặc xem trước 3 ngày)
    private ScoringPeriodStatusEnum scoringPeriodStatus;  // Trạng thái thời gian chấm điểm
}
