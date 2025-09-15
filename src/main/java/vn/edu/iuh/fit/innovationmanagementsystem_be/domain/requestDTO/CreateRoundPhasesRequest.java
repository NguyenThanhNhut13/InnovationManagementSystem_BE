package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoundPhasesRequest {

    @NotNull(message = "ID của InnovationDecision không được để trống")
    private String decisionId;

    @NotNull(message = "Tên đợt sáng kiến không được để trống")
    private String roundName;

    @NotNull(message = "Ngày bắt đầu đợt không được để trống")
    private LocalDate roundStartDate;

    @NotNull(message = "Ngày kết thúc đợt không được để trống")
    private LocalDate roundEndDate;

    private InnovationRoundStatusEnum status;

    @NotEmpty(message = "Danh sách giai đoạn không được để trống")
    @Valid
    private List<InnovationPhaseRequest> phases;
}
