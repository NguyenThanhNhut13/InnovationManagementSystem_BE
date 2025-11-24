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
public class WorkflowStepInfo {
    private String stepName; // "Nộp hồ sơ", "Thư ký Khoa sơ duyệt", ...
    private String description; // "Giảng viên nộp hồ sơ sáng kiến", ...
    private LocalDateTime completedAt; // Nullable nếu chưa hoàn thành
    private Boolean isCompleted; // true/false
    private Boolean isCurrent; // true nếu là step hiện tại
}
