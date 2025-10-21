package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingDeadlineResponse {

    private String id;
    private String title;
    private LocalDate deadlineDate;
    private String formattedDate;
    private Long daysRemaining;
    private String phaseType;
    private String level;
    private String description;
    private Boolean isDeadline;
}
