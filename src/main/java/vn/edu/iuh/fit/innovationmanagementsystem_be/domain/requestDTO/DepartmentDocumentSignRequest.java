package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDocumentSignRequest {

    @NotBlank(message = "HTML content không được để trống")
    private String htmlContentBase64;

    @NotNull(message = "Document type không được để trống")
    private DocumentTypeEnum documentType;

    @NotBlank(message = "Department ID không được để trống")
    private String departmentId;
}
