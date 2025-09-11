package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignatureResponse {

    private String id;
    private DocumentTypeEnum documentType;
    private UserRoleEnum signedAsRole;
    private LocalDateTime signAt;
    private String signatureHash;
    private String documentHash;
    private SignatureStatusEnum status;

    // User information
    private String userId;
    private String userFullName;
    private String userPersonnelId;

    // Innovation information
    private String innovationId;
    private String innovationName;

    // Certificate information
    private String certificateSerial;
    private String certificateIssuer;
    private LocalDateTime certificateValidFrom;
    private LocalDateTime certificateValidTo;
}
