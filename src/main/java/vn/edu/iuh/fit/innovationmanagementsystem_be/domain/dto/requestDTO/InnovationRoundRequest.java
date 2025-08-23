package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

@Data
public class InnovationRoundRequest {
    private String name;

    @NotNull(message = "Ngày bắt đầu không được để trống")

    @FutureOrPresent(message = "Ngày bắt đầu phải từ ngày hiện tại trở đi")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @FutureOrPresent(message = "Ngày kết thúc phải là ngày trong tương lai")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate endDate;

    private InnovationRoundStatusEnum status;

    @NotBlank(message = "ID của quyết định sáng tạo không được để trống")
    private String innovationDecisionId;
}
