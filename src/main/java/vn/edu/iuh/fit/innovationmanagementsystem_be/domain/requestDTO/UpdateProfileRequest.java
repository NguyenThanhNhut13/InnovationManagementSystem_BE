package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 3, max = 255, message = "Họ và tên phải có ít nhất 3 ký tự và tối đa 255 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @Size(max = 255, message = "Trình độ học vấn không được vượt quá 255 ký tự")
    private String qualification;

    @Size(max = 255, message = "Chức danh không được vượt quá 255 ký tự")
    private String title;

    @AssertTrue(message = "Người dùng phải lớn hơn 18 tuổi")
    public boolean isAgeValid() {
        if (dateOfBirth == null) {
            return true;
        }
        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        return dateOfBirth.isBefore(eighteenYearsAgo) || dateOfBirth.isEqual(eighteenYearsAgo);
    }
}
