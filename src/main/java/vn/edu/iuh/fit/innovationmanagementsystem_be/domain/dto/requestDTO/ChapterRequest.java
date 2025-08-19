package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterRequest {

    @NotBlank(message = "Số hiệu chương không được để trống")
    private String chapterNumber;

    @NotBlank(message = "Tiêu đề chương không được để trống")
    private String title;

    @NotNull(message = "ID quyết định không được để trống")
    private String innovationDecisionId;
}
