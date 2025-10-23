package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDataRequest {

    private String id;

    @NotBlank(message = "Field key không được để trống")
    private String fieldKey;

    @NotBlank(message = "Field label không được để trống")
    private String label;

    @NotNull(message = "Field type không được để trống")
    private FieldTypeEnum type;

    @NotNull(message = "Required status không được để trống")
    private Boolean required = false;

    private TableConfigData tableConfig;

    private List<String> options;

    private Boolean repeatable = false;

    private JsonNode referenceConfig;

    @Valid
    private List<FieldDataRequest> children;

    private JsonNode userDataConfig;

    private JsonNode innovationDataConfig;

    private UserRoleEnum signingRole;

}
