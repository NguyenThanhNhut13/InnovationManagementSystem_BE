package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private String id;
    private String fileName;
    // private String fileUrl; // URL tải xuống (presigned URL) - Đã bỏ theo yêu cầu
    private Long fileSize;
    private String createdBy; // Người upload (nếu cần)
}
