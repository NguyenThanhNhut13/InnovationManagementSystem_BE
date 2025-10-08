package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for department innovation statistics")
public class DepartmentInnovationStatisticsResponse {

    @Schema(description = "Department ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String departmentId;

    @Schema(description = "Department name", example = "Khoa Công nghệ thông tin")
    private String departmentName;

    @Schema(description = "Department code", example = "CNTT")
    private String departmentCode;

    @Schema(description = "Total number of innovations in this department", example = "25")
    private Long totalInnovations;

    @Schema(description = "Number of draft innovations", example = "5")
    private Long draftInnovations;

    @Schema(description = "Number of submitted innovations", example = "8")
    private Long submittedInnovations;

    @Schema(description = "Number of approved innovations", example = "10")
    private Long approvedInnovations;

    @Schema(description = "Number of rejected innovations", example = "2")
    private Long rejectedInnovations;
}
