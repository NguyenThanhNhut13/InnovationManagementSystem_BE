
package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInnovationRoundRequest {

    @NotBlank(message = "Tên đợt không được để trống")
    private String name;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationStartDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationEndDate;

    @NotNull(message = "Trạng thái đầu không được để trống")
    private InnovationRoundStatusEnum status;

    private String description;

    @NotBlank(message = "Năm học không được để trống")
    private String academicYear;

    @Valid
    private InnovationDecisionRequest innovationDecision;

    @Valid
    private Set<InnovationPhaseRequest> innovationPhase;

    @AssertTrue(message = "Ngày bắt đầu phải trước ngày kết thúc")
    public boolean isValidDates() {
        if (registrationStartDate == null || registrationEndDate == null) {
            return true;
        }
        return !registrationStartDate.isAfter(registrationEndDate);
    }

}
