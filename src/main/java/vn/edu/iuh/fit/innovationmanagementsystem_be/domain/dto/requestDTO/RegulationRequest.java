package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegulationRequest {

    @NotBlank(message = "Số hiệu điều không được để trống")
    private String clauseNumber;

    @NotBlank(message = "Tiêu đề điều không được để trống")
    private String title;

    @NotBlank(message = "Nội dung điều không được để trống")
    private String content;

    @NotNull(message = "ID quyết định không được để trống")
    private String innovationDecisionId;

    private String chapterId; // Có thể null nếu không thuộc chương nào
}
