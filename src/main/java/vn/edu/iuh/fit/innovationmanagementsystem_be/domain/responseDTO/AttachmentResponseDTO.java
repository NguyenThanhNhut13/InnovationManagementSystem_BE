package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponseDTO {

    private UUID id;

    private UUID initiativeId;

    private String pathUrl;

    private Attachment.AttachmentType type;

    private String fileName;

    private Long fileSize;

    private String mimeType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    // Constructor to convert from Attachment entity
    public AttachmentResponseDTO(Attachment attachment) {
        this.id = attachment.getId();
        this.initiativeId = attachment.getInitiativeId();
        this.pathUrl = attachment.getPathUrl();
        this.type = attachment.getType();
        this.fileName = attachment.getFileName();
        this.fileSize = attachment.getFileSize();
        this.mimeType = attachment.getMimeType();
        this.createdAt = attachment.getCreatedAt();
        this.updatedAt = attachment.getUpdatedAt();
        this.createdBy = attachment.getCreatedBy();
        this.updatedBy = attachment.getUpdatedBy();
    }
}