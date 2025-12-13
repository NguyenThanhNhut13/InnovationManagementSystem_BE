package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationDetail {
    private String memberId; // ID của thành viên báo vi phạm
    private String memberName; // Tên thành viên báo vi phạm
    private String memberRole; // Vai trò: CHU_TICH, THU_KY, THANH_VIEN
    private String violationType; // Loại vi phạm: DUPLICATE, FEASIBILITY, QUALITY
    private String violationReason; // Lý do vi phạm chi tiết
    private LocalDateTime reportedAt; // Thời gian báo vi phạm
}

