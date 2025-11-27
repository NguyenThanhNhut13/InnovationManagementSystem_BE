package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
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

    // BỎ name - BE sẽ tự động generate
    // BỎ innovationIds - BE sẽ tự động lấy từ roundId

    // Optional - Nếu không truyền, BE sẽ tự động xác định từ role của user
    private ReviewLevelEnum reviewCouncilLevel;

    @NotNull(message = "Danh sách thành viên không được để trống")
    @Size(min = 3, message = "Hội đồng phải có ít nhất 3 thành viên")
    @Valid
    private List<CouncilMemberRequest> members;

    // Optional - Nếu không truyền, BE sẽ tự động lấy round hiện tại đang mở
    private String roundId;

    // Optional - Nếu không truyền, BE sẽ tự động lấy từ current user (nếu cấp Khoa)
    private String departmentId;
}
