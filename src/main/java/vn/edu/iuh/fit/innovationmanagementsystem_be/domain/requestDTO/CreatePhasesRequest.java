package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePhasesRequest {

    @NotNull(message = "ID của InnovationDecision không được để trống")
    private String decisionId;

    @NotEmpty(message = "Danh sách giai đoạn không được để trống")
    @Valid
    private List<InnovationPhaseRequest> phases;
}
