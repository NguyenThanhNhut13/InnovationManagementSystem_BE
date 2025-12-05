package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResponse {
    private int totalRecords;
    private int importedCount;
    private int skippedCount;
    private List<SkippedUser> skippedUsers;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkippedUser {
        private String personnelId;
        private String fullName;
        private String email;
        private String reason;
    }
}
