package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentInnovationDetailResponse {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String authorEmail;
    private String departmentName;
    private String academicYear;
    private String roundName;
    private Boolean isScore;
    private InnovationStatusEnum status;
    private LocalDateTime submittedAt;

    private List<CoAuthorResponse> coAuthors;

    private List<TemplateFormDataResponse> templates;

    private List<AttachmentInfo> attachments;
}
