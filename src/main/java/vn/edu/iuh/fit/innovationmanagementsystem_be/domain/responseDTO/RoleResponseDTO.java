package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRoleEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDTO {

    private UUID id;
    private UserRoleEnum roleName;
}