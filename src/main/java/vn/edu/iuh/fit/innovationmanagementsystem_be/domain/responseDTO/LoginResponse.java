package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String id;
    private String personnelId;
    private String fullName;
    private String email;
    private UserStatusEnum status;
    private String department;
    private String qualification;
    private String title;
    private LocalDate dateOfBirth;
    private List<String> roles;
    private Boolean isSecretary; // true nếu user là thư ký của ít nhất 1 council (cả khoa và trường)
    private Boolean isChairman; // true nếu user là chủ tịch của ít nhất 1 council (cả khoa và trường)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String accessToken;
    private String refreshToken;
}
