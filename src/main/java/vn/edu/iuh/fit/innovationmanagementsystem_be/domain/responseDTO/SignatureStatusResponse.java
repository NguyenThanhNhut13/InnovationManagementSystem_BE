package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureStatusResponse {

    private String innovationId;
    private String innovationName;
    private DocumentTypeEnum documentType;
    private boolean isFullySigned; // Đã ký đủ chưa
    private boolean canSubmit; // Có thể SUBMITTED không
    private List<RequiredSignatureInfo> requiredSignatures; // Danh sách chữ ký cần thiết
    private List<DigitalSignatureResponse> completedSignatures; // Danh sách chữ ký đã hoàn thành

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredSignatureInfo {
        private String roleName; // Tên role cần ký
        private String roleCode; // Code của role
        private boolean isSigned; // Đã ký chưa
        private String signedBy; // Ai đã ký (nếu đã ký)
        private String signedAt; // Khi nào ký (nếu đã ký)
    }
}
