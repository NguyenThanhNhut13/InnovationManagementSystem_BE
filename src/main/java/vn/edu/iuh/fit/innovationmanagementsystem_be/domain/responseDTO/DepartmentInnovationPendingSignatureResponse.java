package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for department innovations pending signature list (for table display)
 * Chỉ chứa thông tin cần thiết để hiển thị table, không có templates và formData đầy đủ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentInnovationPendingSignatureResponse {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String status;
    private Boolean isScore;
    private Boolean isCoAuthor;
    private LocalDateTime updatedAt;
    private Long submissionTimeRemainingSeconds;
    
    // Thông tin về signature status cho template 2
    private Boolean hasSignature; // true nếu đã được TRUONG_KHOA ký template 2
    private String template2Id; // ID của template 2 để navigate
    
    // Thông tin về đợt sáng kiến
    private String roundId; // ID của đợt sáng kiến
}

