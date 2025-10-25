package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho Certificate Signing Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CSRRequestDTO {
    private String csrId;
    private String userId;
    private String publicKey;
    private String subjectDN;
    private String email;
    private String phoneNumber;
    private String organization;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private List<String> validationDocuments;
}
