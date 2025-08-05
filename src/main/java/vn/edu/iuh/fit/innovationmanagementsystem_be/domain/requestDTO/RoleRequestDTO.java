package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {

    private UserRoleEnum roleName;
}