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
public class DepartmentDocumentSignResponse {

    private String reportId;
    private Boolean isSigned;
    private String signatureId;
    private String documentHash;
    private String signatureHash;
    private DocumentTypeEnum documentType;
    private String departmentId;
    private String councilId;
    private UserRoleEnum signedAsRole;
    private String signerName;
    private LocalDateTime signedAt;
    private String pdfUrl;
}
