package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCriteriaDetail {

    @NotBlank(message = "ID tiêu chuẩn không được để trống")
    private String criteriaId; // ID tiêu chuẩn (e.g., "1", "2", "3")

    @NotBlank(message = "ID tiêu chí con không được để trống")
    private String selectedSubCriteriaId; // ID tiêu chí con đã chọn (e.g., "1.1", "2.2")

    @NotNull(message = "Điểm không được để trống")
    @Min(value = 0, message = "Điểm phải >= 0")
    private Integer score; // Điểm của tiêu chí con đã chọn

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes; // Ghi chú cho tiêu chuẩn này
}
