package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInnovationRoundRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private InnovationRoundStatusEnum status;
    private String description;
    private Boolean isActive;
    private String academicYear;

    @AssertTrue(message = "Ngày bắt đầu phải trước ngày kết thúc")
    public boolean isValidDates() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }
}
