package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegulationResponse {

    private String id;
    private String clauseNumber;
    private String title;
    private String content;
    private String innovationDecisionId;
    private String chapterId; // Có thể null nếu không thuộc chương nào
}
