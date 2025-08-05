package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRequestDTO {

    private UUID initiativeId; // innovation ID

    private String fileName;

    private Long fileSize;

    private String mimeType;

    private Attachment.AttachmentType type;

    private String createdBy;

    private String updatedBy;

    // Validation annotations
    @jakarta.validation.constraints.NotNull(message = "Initiative ID is required")
    public UUID getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(UUID initiativeId) {
        this.initiativeId = initiativeId;
    }

    @jakarta.validation.constraints.NotBlank(message = "File name is required")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @jakarta.validation.constraints.NotNull(message = "File size is required")
    @jakarta.validation.constraints.Min(value = 1, message = "File size must be greater than 0")
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @jakarta.validation.constraints.NotBlank(message = "MIME type is required")
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}