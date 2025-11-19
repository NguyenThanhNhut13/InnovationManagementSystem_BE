package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignatureProfileRequest {

    @NotBlank(message = "User ID không được để trống")
    private String userId;

    private String pathUrl;

    private String certificateAuthorityId;
}
