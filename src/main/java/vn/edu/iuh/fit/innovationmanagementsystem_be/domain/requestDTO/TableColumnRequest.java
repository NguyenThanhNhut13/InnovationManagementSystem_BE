package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnRequest {

    @NotBlank(message = "Column key không được để trống")
    private String key;

    @NotBlank(message = "Column label không được để trống")
    private String label;

    @NotNull(message = "Column type không được để trống")
    private FieldTypeEnum type;

    private Boolean required = false;
}
