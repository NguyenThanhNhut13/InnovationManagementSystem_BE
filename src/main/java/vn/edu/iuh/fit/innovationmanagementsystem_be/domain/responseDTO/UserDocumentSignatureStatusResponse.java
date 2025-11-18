package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDocumentSignatureStatusResponse {

    private String innovationId;
    private DocumentTypeEnum documentType;
    private UserRoleEnum signedAsRole;
    private boolean signed;
    private LocalDateTime signedAt;
}
