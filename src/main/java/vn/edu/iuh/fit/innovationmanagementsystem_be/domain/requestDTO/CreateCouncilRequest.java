package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouncilRequest {

    @NotBlank(message = "Tên Hội đồng không được để trống")
    @Size(max = 255, message = "Tên Hội đồng không được vượt quá 255 ký tự")
    private String name;

    @NotNull(message = "Cấp độ Hội đồng không được để trống")
    private ReviewLevelEnum reviewCouncilLevel;

    @NotNull(message = "Danh sách thành viên không được để trống")
    @Size(min = 3, message = "Hội đồng phải có ít nhất 3 thành viên")
    @Valid
    private List<CouncilMemberRequest> members;

    private List<String> innovationIds;
}
