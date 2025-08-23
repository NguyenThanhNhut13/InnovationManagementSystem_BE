package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFormTemplateRequest {

    @NotBlank(message = "Tên template không được để trống")
    private String name;

    private String description;

    @NotNull(message = "ID của innovation round không được để trống")
    private String innovationRoundId;
}
