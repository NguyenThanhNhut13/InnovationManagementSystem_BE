package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO;

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
public class MergeDepartmentsRequest {

    @NotEmpty(message = "Danh sách department IDs để gộp không được để trống")
    @Size(min = 2, message = "Phải có ít nhất 2 department để gộp")
    private List<String> sourceDepartmentIds;

    @NotBlank(message = "Tên department mới không được để trống")
    @Size(min = 3, max = 100, message = "Tên department phải từ 3-100 ký tự")
    private String newDepartmentName;

    @NotBlank(message = "Mã department mới không được để trống")
    @Size(min = 2, max = 20, message = "Mã department phải từ 2-20 ký tự")
    private String newDepartmentCode;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    @NotBlank(message = "Người thực hiện gộp không được để trống")
    private String mergedBy;
}