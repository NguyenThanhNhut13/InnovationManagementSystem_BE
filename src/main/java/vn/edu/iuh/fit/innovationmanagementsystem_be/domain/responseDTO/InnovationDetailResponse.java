package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InnovationDetailResponse {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String academicYear;
    private String departmentName;
    private Boolean isScore;
    private InnovationStatusEnum status;
    private String authorEmail;
    private List<CoAuthorResponse> coAuthors;
    private Integer attachmentCount;
}


