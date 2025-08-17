package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordResponse {

    private String message;
    private String userId;
    private String personnelId;
    private String email;
    private String timestamp;
}
