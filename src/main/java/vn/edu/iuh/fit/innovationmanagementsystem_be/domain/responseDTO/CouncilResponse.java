package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilResponse {

    private String id;
    private String name;
    private ReviewLevelEnum reviewCouncilLevel;
    private List<CouncilMemberResponse> members;
    private Integer innovationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
