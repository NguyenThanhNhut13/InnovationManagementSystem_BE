package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentImportResponse {
    private int totalRecords;
    private int importedCount;
    private int skippedCount;
    private List<SkippedDepartment> skippedDepartments;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkippedDepartment {
        private String departmentCode;
        private String departmentName;
        private String reason;
    }
}
