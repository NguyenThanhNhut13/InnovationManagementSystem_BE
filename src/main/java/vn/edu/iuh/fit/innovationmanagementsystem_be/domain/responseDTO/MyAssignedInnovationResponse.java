package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAssignedInnovationResponse {
    private String innovationId;
    private String innovationName;
    private String authorName;
    private String departmentName;
    private InnovationStatusEnum status;
    
    // Fields cho thành viên hội đồng
    private Boolean isScore;              // true = có chấm điểm, false = không chấm điểm
    private Boolean myIsApproved;          // Quyết định của current user (null nếu chưa đánh giá)
    private Boolean canScore;             // true nếu đang trong thời gian chấm điểm
}

