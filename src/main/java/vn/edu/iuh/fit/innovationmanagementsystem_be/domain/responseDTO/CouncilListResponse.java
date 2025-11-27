package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilListResponse {
    private String id;
    private String name;
    private ReviewLevelEnum reviewCouncilLevel;
    private String departmentName; // null nếu cấp trường
    private String roundName; // Tên đợt sáng kiến
    private Integer memberCount; // Số lượng thành viên (không cần list đầy đủ)
    private Integer innovationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

