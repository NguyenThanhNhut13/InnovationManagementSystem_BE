package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarInnovationResponse {
    private String innovationId;
    private String innovationName;
    private double similarityScore;
    private String similarityPercentage;
    private String departmentName;
    private String authorName;
    private String innovationRoundName;
    private InnovationStatusEnum status;
    private LocalDateTime createdAt;
}
