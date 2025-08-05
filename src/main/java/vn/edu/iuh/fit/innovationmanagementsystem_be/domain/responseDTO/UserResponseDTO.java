package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRoleEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String userName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserRoleEnum role;
    private String personnelId;
    private UUID departmentId;
    private String departmentName;
    private String departmentCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}