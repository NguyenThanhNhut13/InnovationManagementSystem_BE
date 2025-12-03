package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberRequest {

    @NotBlank(message = "User ID không được để trống")
    private String userId;

    @NotNull(message = "Vai trò trong Hội đồng không được để trống")
    private CouncilMemberRoleEnum role;
}
