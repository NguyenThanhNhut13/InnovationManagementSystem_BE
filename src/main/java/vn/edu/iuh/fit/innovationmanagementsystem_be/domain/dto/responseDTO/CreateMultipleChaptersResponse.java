package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMultipleChaptersResponse {

    private String innovationDecisionId;
    private List<ChapterResponse> createdChapters;
    private int totalCreated;
    private String message;

    public CreateMultipleChaptersResponse(String innovationDecisionId, List<ChapterResponse> createdChapters) {
        this.innovationDecisionId = innovationDecisionId;
        this.createdChapters = createdChapters;
        this.totalCreated = createdChapters.size();
        this.message = "Tạo thành công " + this.totalCreated + " chương";
    }
}
