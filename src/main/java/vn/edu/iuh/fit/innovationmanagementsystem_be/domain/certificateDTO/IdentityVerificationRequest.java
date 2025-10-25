package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.certificateDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationRequest {

    @NotNull(message = "Thông tin cá nhân không được để trống")
    private PersonalInfo personalInfo;

    @NotNull(message = "Tài liệu không được để trống")
    private List<String> documents;

    @NotBlank(message = "Mã xác minh email không được để trống")
    private String emailVerificationCode;

    @NotBlank(message = "Mã xác minh số điện thoại không được để trống")
    private String phoneVerificationCode;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfo {
        @NotBlank(message = "Họ tên không được để trống")
        private String fullName;

        @NotBlank(message = "Ngày sinh không được để trống")
        private String dateOfBirth;

        @NotBlank(message = "Số CMND/CCCD không được để trống")
        private String nationalId;
    }
}
