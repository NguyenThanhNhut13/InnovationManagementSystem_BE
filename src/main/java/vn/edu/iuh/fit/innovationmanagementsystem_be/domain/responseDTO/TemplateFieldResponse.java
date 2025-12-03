package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TemplateFieldResponse {

    private String label;
    private String fieldType;
    private JsonNode value;
    private JsonNode tableConfig; // For TABLE fields to render thead

    // Constructor for backward compatibility (without tableConfig)
    public TemplateFieldResponse(String label, String fieldType, JsonNode value) {
        this.label = label;
        this.fieldType = fieldType;
        this.value = value;
        this.tableConfig = null;
    }

    // Constructor with tableConfig
    public TemplateFieldResponse(String label, String fieldType, JsonNode value, JsonNode tableConfig) {
        this.label = label;
        this.fieldType = fieldType;
        this.value = value;
        this.tableConfig = tableConfig;
    }

}
