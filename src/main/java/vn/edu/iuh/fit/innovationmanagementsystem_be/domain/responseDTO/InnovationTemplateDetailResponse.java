package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response cho innovation template detail với đầy đủ template content và formData
 * Dùng cho TRUONG_KHOA ký innovation template (Mẫu 2)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InnovationTemplateDetailResponse extends CreateTemplateWithFieldsResponse {
    
    /**
     * Map fieldKey -> data (formData đã được build sẵn)
     * - SECTION fields: List<Map<String, Object>> (array of instance data)
     * - TABLE fields: List<Map<String, Object>> (array of row data)
     * - Các field types khác: Object
     */
    private Map<String, Object> fieldDataMap;
    
    /**
     * Danh sách chữ ký hiện có của template này
     */
    private List<FormSignatureResponse> templateSignatures;
    
    /**
     * Số giây đã trễ từ deadline (dương nếu đã quá deadline, 0 nếu chưa quá deadline, null nếu không có deadline)
     */
    private Long submissionTimeRemainingSeconds;
}

