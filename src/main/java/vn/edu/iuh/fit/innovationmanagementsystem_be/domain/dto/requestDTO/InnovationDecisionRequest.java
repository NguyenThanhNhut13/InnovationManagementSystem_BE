package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDecisionRequest {

    @NotBlank(message = "Số hiệu quyết định không được để trống")
    private String decisionNumber;

    @NotBlank(message = "Tiêu đề quyết định không được để trống")
    private String title;

    @NotNull(message = "Ngày ban hành không được để trống")
    private LocalDate promulgatedDate;

    @NotBlank(message = "Tên người ký không được để trống")
    private String signedBy;

    private String bases;

    private Integer yearDecision;
}
