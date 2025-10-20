package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingDeadlinesResponse {

    private List<UpcomingDeadlineResponse> upcomingDeadlines;
    private Integer totalDeadlines;
    private String currentRoundName;
    private String academicYear;
}
