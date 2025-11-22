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
    // Thông tin cơ bản
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String authorEmail;
    private String departmentName;
    private String academicYear;
    private Boolean isScore;
    private InnovationStatusEnum status;
    private LocalDateTime submittedAt; // Ngày nộp

    // Danh sách đồng tác giả
    private List<CoAuthorResponse> coAuthors;

    // Nội dung chi tiết (form data)
    private List<TemplateFormDataResponse> templates;

    // Tài liệu đính kèm
    private List<AttachmentResponse> attachments;
}
