package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponseDTO {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private UUID roleId;
    private String roleName;
}