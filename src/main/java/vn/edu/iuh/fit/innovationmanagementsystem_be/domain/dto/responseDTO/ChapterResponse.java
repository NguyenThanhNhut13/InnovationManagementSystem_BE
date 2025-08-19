package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponse {

    private String id;
    private String chapterNumber;
    private String title;
    private String innovationDecisionId;

    // Có thể thêm danh sách regulations nếu cần
    private List<String> regulationIds;
}
