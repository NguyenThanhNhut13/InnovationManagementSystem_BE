package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InnovationPhaseRequest {

    private String name;

    @NotNull(message = "Loại giai đoạn không được để trống")
    private InnovationPhaseTypeEnum phaseType;

    @NotNull(message = "Ngày bắt đầu giai đoạn không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate phaseStartDate;

    @NotNull(message = "Ngày kết thúc giai đoạn không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate phaseEndDate;

    private String description;

    @NotNull(message = "Cấp độ của giai đoạn không được để trống")
    private InnovationPhaseLevelEnum level;

    @NotNull(message = "Thứ tự giai đoạn không được để trống")
    @Min(value = 0, message = "Thứ tự phase phải >= 0")
    private Integer phaseOrder;

    private Boolean isDeadline = false;
}
