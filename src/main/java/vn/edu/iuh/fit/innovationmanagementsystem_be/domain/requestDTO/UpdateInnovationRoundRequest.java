package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

@Data
public class UpdateInnovationRoundRequest {
    private String name;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate endDate;

    private InnovationRoundStatusEnum status;

    private String innovationDecisionId;
}

