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
public class ImportMultipleRegulationsRequest {

    @NotBlank(message = "ID chương không được để trống")
    private String chapterId;

    @NotEmpty(message = "Danh sách điều khoản không được để trống")
    @Valid
    private List<RegulationData> regulations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegulationData {
        @NotBlank(message = "Số điều không được để trống")
        private String clauseNumber;

        @NotBlank(message = "Tiêu đề điều khoản không được để trống")
        private String title;

        private String content;
    }
}
