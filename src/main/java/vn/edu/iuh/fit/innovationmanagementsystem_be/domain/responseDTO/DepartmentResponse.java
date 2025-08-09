package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private String id;
    private String departmentName;
    private String departmentCode;
    private int totalUsers; // Số lượng người dùng trong khoa
    private int totalInnovations; // Số lượng sáng kiến của khoa
}
