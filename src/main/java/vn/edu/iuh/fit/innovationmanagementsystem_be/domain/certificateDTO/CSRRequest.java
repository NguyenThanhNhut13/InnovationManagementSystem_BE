package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CSRRequest {

    @NotBlank(message = "Subject DN không được để trống")
    private String subjectDN; // CN=John Doe, OU=IT, O=Company, C=VN

    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotBlank(message = "Tổ chức không được để trống")
    private String organization;

    @NotNull(message = "Tài liệu xác minh không được để trống")
    private List<String> validationDocuments; // ["CMND", "CCCD", "Passport"]
}
