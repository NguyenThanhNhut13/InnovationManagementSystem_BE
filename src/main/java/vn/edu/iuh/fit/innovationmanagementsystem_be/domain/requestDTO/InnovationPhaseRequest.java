package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InnovationPhaseRequest {

    private String name;

    private InnovationRoundStatusEnum status;

    // Thông tin giai đoạn cụ thể
    @NotNull(message = "Loại giai đoạn không được để trống")
    private InnovationPhaseEnum phaseType;

    @NotNull(message = "Ngày bắt đầu giai đoạn không được để trống")
    private LocalDate phaseStartDate;

    @NotNull(message = "Ngày kết thúc giai đoạn không được để trống")
    private LocalDate phaseEndDate;

    private String description;

    private Boolean isActive;

    @NotNull(message = "Thứ tự giai đoạn không được để trống")
    private Integer phaseOrder;
}
