package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMultipleChaptersRequest {

    @NotBlank(message = "ID quyết định không được để trống")
    private String innovationDecisionId;

    @NotEmpty(message = "Danh sách chương không được để trống")
    @Valid
    private List<ChapterData> chapters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterData {
        @NotBlank(message = "Số hiệu chương không được để trống")
        private String chapterNumber;

        @NotBlank(message = "Tiêu đề chương không được để trống")
        private String title;
    }
}
