package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRoundRequest {

    @NotBlank(message = "Tên đợt không được để trống")
    private String name;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private InnovationRoundStatusEnum status;

    private String description;

    private Boolean isActive;

    @NotBlank(message = "Năm học không được để trống")
    private String academicYear;

    @NotBlank(message = "ID của InnovationDecision không được để trống")
    private String decisionId;
}
