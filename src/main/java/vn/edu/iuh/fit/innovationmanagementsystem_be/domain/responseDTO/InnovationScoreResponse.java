package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ScoreCriteriaDetail;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InnovationScoreResponse {

    private String reviewScoreId;
    private String innovationId;
    private String innovationName;

    // Reviewer info
    private String reviewerName;
    private String reviewerEmail;

    // Scoring
    private List<ScoreCriteriaDetail> scoringDetails;
    private Integer totalScore;
    private Integer maxTotalScore; // 100

    // Decision
    private Boolean isApproved;
    private Boolean requiresSupplementaryDocuments;
    private String detailedComments;

    // Timestamps
    private LocalDateTime reviewedAt;
}
