package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPhaseRequest {

    @NotNull(message = "Tên giai đoạn không được để trống")
    private String name;

    @NotNull(message = "Loại giai đoạn không được để trống")
    private InnovationPhaseTypeEnum phaseType;

    @NotNull(message = "Thứ tự giai đoạn không được để trống")
    private Integer phaseOrder;

    @NotNull(message = "Ngày bắt đầu giai đoạn không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate phaseStartDate;

    @NotNull(message = "Ngày kết thúc giai đoạn không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate phaseEndDate;

    private String description;

    private InnovationRoundStatusEnum status;
}
