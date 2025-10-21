package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberResponse {
    private String id;
    private String userId;
    private String userFullName;
    private String userEmail;
    private String userPersonnelId;
    private String departmentName;
}
