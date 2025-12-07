package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTemplateFormDataResponse {

    private String templateId;
    private Map<String, Object> formData;

}

