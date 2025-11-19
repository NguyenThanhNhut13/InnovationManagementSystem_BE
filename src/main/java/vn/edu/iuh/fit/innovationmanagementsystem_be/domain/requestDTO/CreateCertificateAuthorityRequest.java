package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateAuthorityRequest {

    @NotBlank(message = "Tên CA không được để trống")
    private String name;

    @NotBlank(message = "Certificate data không được để trống")
    private String certificateData;

    private String description;
}
