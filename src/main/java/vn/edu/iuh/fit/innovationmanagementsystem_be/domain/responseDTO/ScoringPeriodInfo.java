package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ScoringPeriodStatusEnum;

import java.time.LocalDate;

/**
 * DTO chứa thông tin về thời gian chấm điểm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringPeriodInfo {
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean canScore;
    private boolean canView;
    private ScoringPeriodStatusEnum status;
}

