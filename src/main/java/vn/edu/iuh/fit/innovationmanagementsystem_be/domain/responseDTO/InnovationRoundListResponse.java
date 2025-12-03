package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRoundListResponse {
    private String id;
    private String name;
    private String academicYear;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private Integer phaseCount;
    private Integer criteriaCount;
    private InnovationRoundStatusEnum status;
}
