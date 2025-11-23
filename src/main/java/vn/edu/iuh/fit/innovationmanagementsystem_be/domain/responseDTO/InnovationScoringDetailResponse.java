package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class InnovationScoringDetailResponse extends DepartmentInnovationDetailResponse {
    
    private JsonNode scoringCriteria;  // Bảng điểm từ InnovationDecision
    private Integer maxTotalScore;      // Tổng điểm tối đa (thường = 100)
}
