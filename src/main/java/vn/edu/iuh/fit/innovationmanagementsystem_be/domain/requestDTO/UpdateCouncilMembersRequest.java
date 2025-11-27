package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCouncilMembersRequest {

    @NotNull(message = "Danh sách thành viên không được để trống")
    @Size(min = 3, message = "Hội đồng phải có ít nhất 3 thành viên")
    @Valid
    private List<CouncilMemberRequest> members;
}

