package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDigitalSignatureRequest {

    @NotBlank(message = "Document hash không được để trống")
    private String documentHash;

    @NotBlank(message = "Signature hash không được để trống")
    private String signatureHash;

    @NotNull(message = "User ID không được để trống")
    private String userId;
}
