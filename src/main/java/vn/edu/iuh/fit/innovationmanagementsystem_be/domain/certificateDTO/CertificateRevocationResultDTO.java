package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO cho kết quả thu hồi certificate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRevocationResultDTO {
    private String certificateId;
    private String status;
    private String message;
    private LocalDateTime revokedAt;
    private String reason;
}
