package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

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
    private int userCount;
    private int innovationCount;
}
