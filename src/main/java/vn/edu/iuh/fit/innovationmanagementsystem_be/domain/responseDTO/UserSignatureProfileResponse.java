package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignatureProfileResponse {
    private String id;
    private String pathUrl;
    private String publicKey;
    private String certificateSerial;
    private String certificateIssuer;
    private String certificateData;
    private String certificateChain;
    private LocalDateTime certificateExpiryDate;
    private String certificateStatus;
    private LocalDateTime lastCertificateValidation;

    // User information
    private String userId;
    private String userFullName;
    private String userPersonnelId;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
