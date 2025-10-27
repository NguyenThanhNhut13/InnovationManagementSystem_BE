package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormDataRequest {

    @NotNull(message = "Field value không được để trống")
    private JsonNode fieldValue;

    @NotBlank(message = "Form field ID không được để trống")
    private String formFieldId;

    @NotBlank(message = "Innovation ID không được để trống")
    private String innovationId;

}
