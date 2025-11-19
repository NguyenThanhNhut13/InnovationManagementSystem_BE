package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateAuthorityResponse {
    private String id;
    private String name;
    private String certificateSerial;
    private String certificateIssuer;
    private String certificateSubject;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private CAStatusEnum status;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean expired;
    private long daysUntilExpiry;
}
