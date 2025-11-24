package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityHistoryInfo {
    private String actionName; // "Nộp hồ sơ", "Sơ duyệt hồ sơ", ...
    private String fromStatus; // "Bản nháp", "Đã nộp", ...
    private String toStatus; // "Đã nộp", "Chờ khoa đánh giá", ...
    private String actorName; // "Nguyễn Văn A", "Nguyễn Thị Thư ký", ...
    private String actorRole; // "GIANG_VIEN", "QUAN_TRI_VIEN_KHOA", ...
    private LocalDateTime timestamp;
    private String notes; // "Giảng viên nộp hồ sơ sáng kiến lần đầu", ...
}
