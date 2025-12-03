package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDetailForGiangVienResponse {
    // Tab 1: Tổng quan
    private OverviewData overview;
    private List<CoAuthorInfo> coAuthors;
    private StatisticsData statistics;

    // Tab 2: Nội dung Form (sử dụng existing)
    private InnovationFormDataResponse formData;

    // Tab 3: Tài liệu
    private List<AttachmentInfo> attachments;

    // Tab 4: Đánh giá
    private List<ReviewCommentInfo> reviewComments;

    // Tab 5: Quy trình
    private List<WorkflowStepInfo> workflowSteps;

    // Tab 6: Lịch sử
    private List<ActivityHistoryInfo> activityHistory;
}
