package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class VerifyDigitalSignatureResponse {

    private boolean verified;
    private DocumentTypeEnum documentType;
    private UserRoleEnum signedAsRole;

    // Thông tin người ký
    private String userId;
    private String userFullName;
    private String userPersonnelId;

    // Thời gian ký
    private LocalDateTime signAt;

    // Thông tin sáng kiến (nếu có)
    private String innovationId;
    private String innovationName;
}
