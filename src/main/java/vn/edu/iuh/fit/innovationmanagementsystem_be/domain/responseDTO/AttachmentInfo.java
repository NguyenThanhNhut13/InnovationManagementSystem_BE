package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentInfo {
    private String fileName;
    private String templateId;
    private String templateType;
    private LocalDateTime uploadedAt;
    private Boolean isDigitallySigned;
    private String signerName;
}
