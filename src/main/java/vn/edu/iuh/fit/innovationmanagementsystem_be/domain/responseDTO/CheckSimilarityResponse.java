package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckSimilarityResponse {
    private boolean hasSimilar;
    private int totalFound;
    private double threshold;
    private List<SimilarInnovationResponse> similarInnovations;
}
