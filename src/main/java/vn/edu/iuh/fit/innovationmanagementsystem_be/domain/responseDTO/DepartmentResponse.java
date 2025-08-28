package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private String departmentId;
    private String departmentName;
    private String departmentCode;
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long suspendedUsers;
    private int innovationCount;

}
