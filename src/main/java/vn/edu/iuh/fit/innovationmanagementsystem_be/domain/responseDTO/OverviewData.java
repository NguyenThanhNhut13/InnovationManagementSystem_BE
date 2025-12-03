package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewData {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String departmentName;
    private String academicYear;
    private String status;
    private String roundName;
    private Boolean isScored; // Có chấm điểm hay không
}
