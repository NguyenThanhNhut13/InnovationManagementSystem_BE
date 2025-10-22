package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldData {

    private String id;

    @NotBlank(message = "Field key không được để trống")
    private String fieldKey;

    @NotBlank(message = "Field label không được để trống")
    private String label;

    @NotNull(message = "Field type không được để trống")
    private FieldTypeEnum type;

    @NotNull(message = "Required status không được để trống")
    private Boolean required = false;

    private String placeholder;

    private TableConfigData tableConfig;

    private List<String> options;

    private Boolean repeatable = false;

    @Valid
    private ReferenceConfig referenceConfig;

    @Valid
    private List<FieldData> children;
}
