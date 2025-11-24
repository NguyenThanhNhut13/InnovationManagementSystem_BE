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
public class ReviewCommentInfo {
    private String reviewerName;
    private String reviewerRole; // "Thành viên hội đồng"
    private String level; // "Cấp Khoa" / "Cấp Trường"
    private LocalDateTime createdAt;
    private String content;
}
