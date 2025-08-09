package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "Tên khoa/viện không được để trống")
    @Size(max = 255, message = "Tên khoa/viện không được vượt quá 255 ký tự")
    private String departmentName;

    @NotBlank(message = "Mã khoa/viện không được để trống")
    @Size(max = 50, message = "Mã khoa/viện không được vượt quá 50 ký tự")
    private String departmentCode;
}
