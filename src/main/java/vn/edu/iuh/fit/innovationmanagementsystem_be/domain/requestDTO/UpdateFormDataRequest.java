package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFormDataRequest {

    private JsonNode fieldValue;

    private String formFieldId;

    private String innovationId;
}
