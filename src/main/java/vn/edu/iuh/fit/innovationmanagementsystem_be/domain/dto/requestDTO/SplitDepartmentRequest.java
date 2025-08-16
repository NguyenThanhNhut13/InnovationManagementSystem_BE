package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SplitDepartmentRequest {

    @NotBlank(message = "ID department cần tách không được để trống")
    private String sourceDepartmentId;

    @NotEmpty(message = "Danh sách department mới không được để trống")
    @Size(min = 2, message = "Phải tách thành ít nhất 2 department")
    @Valid
    private List<NewDepartmentInfo> newDepartments;

    @NotBlank(message = "Người thực hiện tách không được để trống")
    private String splitBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewDepartmentInfo {
        @NotBlank(message = "Tên department mới không được để trống")
        @Size(min = 3, max = 100, message = "Tên department phải từ 3-100 ký tự")
        private String departmentName;

        @NotBlank(message = "Mã department mới không được để trống")
        @Size(min = 2, max = 20, message = "Mã department phải từ 2-20 ký tự")
        private String departmentCode;

        @NotEmpty(message = "Danh sách user IDs cần chuyển không được để trống")
        private List<String> userIds;

        @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
        private String description;
    }
}