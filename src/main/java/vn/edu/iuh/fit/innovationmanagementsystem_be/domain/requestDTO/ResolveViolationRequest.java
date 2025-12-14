package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolveViolationRequest {
    @NotNull(message = "Quyết định không được để trống")
    private Boolean dismissViolation; // true = Bỏ qua cảnh báo, false = Từ chối (loại sáng kiến)
}

