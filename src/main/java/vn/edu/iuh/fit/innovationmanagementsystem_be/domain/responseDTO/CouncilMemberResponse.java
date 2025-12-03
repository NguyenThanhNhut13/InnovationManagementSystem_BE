package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberResponse {

    private String id;
    private String userId;
    private String fullName;
    private String email;
    private CouncilMemberRoleEnum role;
}
