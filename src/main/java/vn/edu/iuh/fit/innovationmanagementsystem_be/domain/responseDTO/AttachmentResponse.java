package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import java.time.LocalDateTime;

import lombok.Data;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

@Data
public class AttachmentResponse {

    private String id;
    private String pathUrl;
    private AttachmentTypeEnum type;
    private String fileName;
    private Long fileSize;
    private String templateId;
    private DocumentTypeEnum documentType;
    private String innovationId;
    private String innovationName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
