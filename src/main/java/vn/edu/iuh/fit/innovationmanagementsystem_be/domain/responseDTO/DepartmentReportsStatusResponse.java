package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReportsStatusResponse {
    /**
     * Trạng thái của Mẫu 3: Biên bản họp đơn vị đánh giá sáng kiến
     */
    private ReportStatusResponse meetingMinutes;
    
    /**
     * Trạng thái của Mẫu 4: Tổng hợp đề nghị công nhận sáng kiến (không chấm điểm)
     */
    private ReportStatusResponse proposalSummary;
    
    /**
     * Trạng thái của Mẫu 5: Tổng hợp đề nghị công nhận sáng kiến (có chấm điểm)
     */
    private ReportStatusResponse scoringSummary;
}

