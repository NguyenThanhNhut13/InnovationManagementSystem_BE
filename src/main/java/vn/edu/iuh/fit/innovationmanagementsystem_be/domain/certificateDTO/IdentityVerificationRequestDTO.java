package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * DTO cho yêu cầu xác minh danh tính
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationRequestDTO {
    private PersonalInfoDTO personalInfo;
    private List<String> documents;
    private String emailVerificationCode;
    private String phoneVerificationCode;
}
