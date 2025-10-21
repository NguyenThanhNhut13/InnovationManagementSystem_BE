package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilRequest {

    @NotBlank(message = "Tên hội đồng không được để trống")
    private String name;

    @NotNull(message = "Cấp độ đánh giá không được để trống")
    private ReviewLevelEnum reviewCouncilLevel;

    private List<String> memberIds;
}
