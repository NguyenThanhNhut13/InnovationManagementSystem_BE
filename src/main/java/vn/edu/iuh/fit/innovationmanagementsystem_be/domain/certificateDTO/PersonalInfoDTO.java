package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO cho thông tin cá nhân trong quy trình xác minh CA
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfoDTO {
    private String fullName;
    private String dateOfBirth;
    private String nationalId;
}
