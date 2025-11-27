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

    // BỎ name - BE sẽ tự động generate

    // Optional - Nếu không truyền, hệ thống sẽ tự động gắn dựa trên role của user
    private ReviewLevelEnum reviewCouncilLevel;

    @NotNull(message = "Danh sách thành viên không được để trống")
    @Size(min = 3, message = "Hội đồng phải có ít nhất 3 thành viên")
    @Valid
    private List<CouncilMemberRequest> members;

    // BỎ innovationIds - BE sẽ tự động lấy từ roundId

    // Required - Round ID để BE tự động lấy eligible innovations và generate name
    @NotBlank(message = "Round ID không được để trống")
    private String roundId;

    // Optional - Department ID (chỉ cần cho cấp Khoa, cấp Trường thì null)
    private String departmentId;
}
