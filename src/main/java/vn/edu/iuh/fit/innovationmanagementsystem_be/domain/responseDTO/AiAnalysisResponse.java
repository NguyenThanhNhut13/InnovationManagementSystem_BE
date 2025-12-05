package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {
    private String innovationId;
    private String innovationName;

    // Tóm tắt
    private String summary;
    private List<String> keyPoints;

    // Đánh giá chi tiết
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private String analysis;

    private LocalDateTime generatedAt;
}
