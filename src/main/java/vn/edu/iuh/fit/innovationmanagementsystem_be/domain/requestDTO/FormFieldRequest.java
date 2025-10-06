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
public class FormFieldRequest {

    @NotBlank(message = "Label không được để trống")
    private String label;

    @NotBlank(message = "Field key không được để trống")
    private String fieldKey;

    @NotNull(message = "Field type không được để trống")
    private FieldTypeEnum fieldType;

    @NotNull(message = "Trạng thái required không được để trống")
    private Boolean required = false;

    private String placeholder;

}