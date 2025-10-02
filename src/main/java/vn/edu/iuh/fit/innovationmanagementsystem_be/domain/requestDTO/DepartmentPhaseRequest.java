package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPhaseRequest {

    @NotNull(message = "Loại giai đoạn không được để trống")
    private InnovationPhaseTypeEnum phaseType;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private String description;

    @NotNull(message = "Thứ tự giai đoạn không được để trống")
    private Integer phaseOrder;

    @NotNull(message = "ID của InnovationPhase không được để trống")
    private String innovationPhaseId; // Giai đoạn tổng thể mà giai đoạn khoa này thuộc về
}
