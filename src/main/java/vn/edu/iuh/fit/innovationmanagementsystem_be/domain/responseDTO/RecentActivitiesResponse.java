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
public class RecentActivitiesResponse {

    private List<ActivityResponse> activities;
    private Long totalActivities;
    private Long unreadCount;
    private Boolean hasMore;
    private Integer currentPage;
    private Integer totalPages;
}
