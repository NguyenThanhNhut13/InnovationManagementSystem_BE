package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDecisionResponse {

    private String id;
    private String decisionNumber;
    private String title;
    private LocalDate promulgatedDate;
    private String signedBy;
    private String bases;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private String createdBy;
    private String updatedBy;

    // Có thể thêm danh sách chapters và regulations nếu cần
    private List<String> chapterIds;
    private List<String> regulationIds;
}
