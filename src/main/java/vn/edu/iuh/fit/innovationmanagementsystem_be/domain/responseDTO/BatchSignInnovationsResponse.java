package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchSignInnovationsResponse {

    private int totalRequested;
    private int successCount;
    private int failedCount;
    private List<SignResultItem> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignResultItem {
        private String innovationId;
        private String innovationTitle;
        private boolean success;
        private String message;
        private String signatureHash;
    }
}
