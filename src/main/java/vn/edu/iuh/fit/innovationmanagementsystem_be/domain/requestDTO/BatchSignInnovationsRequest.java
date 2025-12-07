package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

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
public class BatchSignInnovationsRequest {

    @NotEmpty(message = "Danh sách sáng kiến không được để trống")
    @Valid
    private List<BatchSignInnovationItem> innovations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchSignInnovationItem {
        @NotBlank(message = "Innovation ID không được để trống")
        private String innovationId;

        @NotBlank(message = "HTML content không được để trống")
        private String htmlContentBase64;
    }
}
