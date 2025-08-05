package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRoleEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    private String userName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private UserRoleEnum role;
    private String personnelId;
    private UUID departmentId;
}