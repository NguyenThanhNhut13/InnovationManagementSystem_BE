package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class InnovationScoringDetailResponse extends DepartmentInnovationDetailResponse {
    
    private JsonNode scoringCriteria;  // Bảng điểm từ InnovationDecision
    private Integer maxTotalScore;      // Tổng điểm tối đa (thường = 100)
    private List<SimilarInnovationWarning> similarityWarnings = new ArrayList<>();
    private Boolean hasSimilarityWarning = false;
    
    // Note: Scoring period information (scoringStartDate, scoringEndDate, canScore, canView, scoringPeriodStatus)
    // đã được di chuyển sang CouncilResponse để tránh lặp lại cho mỗi innovation
}
