package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String userId;
    private String personnelId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserRoleEnum role;
    private String departmentId;
    private String departmentName;
}
