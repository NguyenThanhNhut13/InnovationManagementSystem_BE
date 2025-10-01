package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
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

    private String id;

    @NotBlank(message = "Số hiệu quyết định không được để trống")
    private String decisionNumber;

    @NotBlank(message = "Tiêu đề quyết định không được để trống")
    private String title;

    @NotNull(message = "Ngày ban hành không được để trống")
    private LocalDate promulgatedDate;

    @NotBlank(message = "Tên file không được để trống")
    private String fileName;

    @NotBlank(message = "Tiêu chí chấm điểm không được để trống")
    private JsonNode scoringCriteria;

    @NotBlank(message = "Nội dung hướng dẫn không được để trống")
    private String contentGuide;

}
