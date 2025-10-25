package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO cho certificate đã cấp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuedCertificateDTO {
    private String certificateId;
    private String userId;
    private String csrId;
    private String certificateData;
    private String certificateChain;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String status;
    private LocalDateTime revokedAt;
    private String revocationReason;
}
