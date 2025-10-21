package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InnovationAcademicYearStatisticsDTO {

    private List<AcademicYearData> academicYearData;
    private long totalInnovations;
    private int totalAcademicYears;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicYearData {
        private String academicYear;
        private long totalInnovations;
        private long submittedInnovations;
        private long approvedInnovations;
        private long rejectedInnovations;
        private long pendingInnovations;

        // Phần trăm
        private double approvedPercentage;
        private double rejectedPercentage;
        private double pendingPercentage;
    }
}
