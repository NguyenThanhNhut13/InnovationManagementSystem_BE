package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Mã nhân sự không được để trống")
    @Pattern(regexp = "^[0-9]{8}$", message = "Mã nhân sự chỉ được chứa số và phải có đúng 8 chữ số")
    private String personnelId;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 3, max = 255, message = "Họ và tên phải có ít nhất 3 ký tự và tối đa 255 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "ID phòng ban không được để trống")
    private String departmentId;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @Size(max = 255, message = "Trình độ học vấn không được vượt quá 255 ký tự")
    private String qualification;

    @Size(max = 255, message = "Chức danh không được vượt quá 255 ký tự")
    private String title;

    private UserStatusEnum status = UserStatusEnum.ACTIVE;
}
