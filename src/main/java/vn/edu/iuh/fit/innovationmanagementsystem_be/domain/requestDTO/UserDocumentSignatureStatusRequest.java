package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDocumentSignatureStatusRequest {

    private String innovationId;
    private DocumentTypeEnum documentType;
    private UserRoleEnum signedAsRole;
}
