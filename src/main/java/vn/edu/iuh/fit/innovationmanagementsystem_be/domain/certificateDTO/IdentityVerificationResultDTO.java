package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * DTO cho kết quả xác minh danh tính
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationResultDTO {
    private String csrId;
    private String userId;
    private String status;
    private String message;
    private List<String> verificationSteps;
    private List<String> errors;
}
