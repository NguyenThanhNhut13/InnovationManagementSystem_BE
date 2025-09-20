package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignatureRequest {

    @NotBlank(message = "Innovation ID không được để trống")
    private String innovationId;

    @NotNull(message = "Document type không được để trống")
    private DocumentTypeEnum documentType;

    @NotNull(message = "Signed as role không được để trống")
    private UserRoleEnum signedAsRole;

    @NotBlank(message = "Document hash không được để trống")
    private String documentHash; // Hash của tài liệu trước khi ký

    @NotBlank(message = "Signature hash không được để trống")
    private String signatureHash; // Hash của chữ ký

    private String certificateSerial; // Số serial của chứng chỉ (optional)

    private String certificateIssuer; // Tổ chức phát hành chứng chỉ (optional)
}
