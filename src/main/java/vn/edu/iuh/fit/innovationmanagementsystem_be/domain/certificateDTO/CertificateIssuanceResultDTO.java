package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO cho kết quả cấp certificate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssuanceResultDTO {
    private String certificateId;
    private String status;
    private String message;
    private String certificateData;
    private String certificateChain;
    private LocalDateTime expiresAt;
}
