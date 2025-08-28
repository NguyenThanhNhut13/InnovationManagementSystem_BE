package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDepartmentResponse {
    private String userId;
    private String personnelId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserStatusEnum status;
    private String departmentId;
    private String departmentName;
    private String departmentCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}