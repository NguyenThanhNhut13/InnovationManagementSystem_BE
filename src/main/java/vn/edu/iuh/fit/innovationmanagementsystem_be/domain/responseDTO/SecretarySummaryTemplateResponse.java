package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SecretarySummaryTemplateResponse extends CreateTemplateWithFieldsResponse {
    
    /**
     * Map fieldKey -> data
     * - SECTION fields: List<Map<String, Object>> (array of instance data)
     * - TABLE fields: List<Map<String, Object>> (array of row data)
     * - Các field types khác: có thể thêm sau nếu cần
     */
    private Map<String, Object> fieldDataMap;
}


