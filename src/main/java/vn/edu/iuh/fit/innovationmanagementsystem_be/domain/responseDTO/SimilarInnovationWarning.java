package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarInnovationWarning {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String departmentName;
    private String status;
    private Double similarityScore; // 0.0 - 1.0
    private String riskLevel; // HIGH (>0.85), MEDIUM (0.75-0.85), LOW (0.70-0.75)
}

