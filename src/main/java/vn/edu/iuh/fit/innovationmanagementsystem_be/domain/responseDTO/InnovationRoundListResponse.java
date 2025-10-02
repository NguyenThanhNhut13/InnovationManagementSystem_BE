package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRoundListResponse {
    private String id;
    private String name; // Đợt sáng kiến
    private String academicYear; // Năm học
    private LocalDate registrationStartDate; // Thời gian bắt đầu
    private LocalDate registrationEndDate; // Thời gian kết thúc
    private Integer phaseCount; // Giai đoạn (số lượng phases)
    private Integer criteriaCount; // Tiêu chí (số lượng criteria trong scoringCriteria)
    private InnovationRoundStatusEnum status; // Trạng thái
}
