package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDocumentSignRequest {

    @NotBlank(message = "Template ID không được để trống")
    private String templateId;

    private Map<String, Object> reportData;

    @NotBlank(message = "HTML content không được để trống")
    private String htmlContentBase64;

    @NotNull(message = "Document type không được để trống")
    private DocumentTypeEnum documentType;

    // departmentId và councilId sẽ được tự động lấy từ current user và current council
    private String departmentId;

    private String councilId;

    // Nếu true: ký ngay và tạo DigitalSignature
    // Nếu false: chỉ lưu Report, ký sau
    private Boolean isSign = false;
}
