package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SimilarInnovationDetailResponse extends DepartmentInnovationDetailResponse {
    
    // Thêm 2 fields mới cho similarity
    private Double similarityScore;  // Độ tương tự (0.0 - 1.0)
    private String riskLevel;        // Mức độ rủi ro: HIGH, MEDIUM, LOW
}

