package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response cho innovation templates detail với đầy đủ template content và formData cho cả Mẫu 1 và Mẫu 2
 * Dùng cho TRUONG_KHOA ký innovation template (Mẫu 2) và xem Mẫu 1 (readonly)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationTemplatesForSigningResponse {
    
    /**
     * Thông tin cơ bản của innovation
     */
    private InnovationBasicInfo innovation;
    
    /**
     * Template 1 (DON_DE_NGHI) - Readonly, không cần signatures
     */
    private InnovationTemplateDetailResponse template1;
    
    /**
     * Template 2 (BAO_CAO_MO_TA) - Có signatures để ký
     */
    private InnovationTemplateDetailResponse template2;
    
    /**
     * Số giây đã trễ từ deadline (dương nếu đã quá deadline, 0 nếu chưa quá deadline, null nếu không có deadline)
     */
    private Long submissionTimeRemainingSeconds;
}

