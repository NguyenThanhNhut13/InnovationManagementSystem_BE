package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ActivityResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RecentActivitiesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlinesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ActivityLogService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Log", description = "API quản lý hoạt động và thông báo")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final InnovationService innovationService;
    private final UserService userService;

    // 1. Lấy hạn chót sắp tới - OK
    @GetMapping("/upcoming-deadlines")
    @ApiMessage("Lấy danh sách hạn chót sắp tới thành công")
    @Operation(summary = "Get Upcoming Deadlines", description = "Get upcoming deadlines from current active innovation round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming deadlines retrieved successfully", content = @Content(schema = @Schema(implementation = UpcomingDeadlinesResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "No active round found")
    })
    public ResponseEntity<UpcomingDeadlinesResponse> getUpcomingDeadlines() {
        UpcomingDeadlinesResponse deadlines = innovationService.getUpcomingDeadlines();
        return ResponseEntity.ok(deadlines);
    }

    // 2. Lấy hoạt động gần đây - OK
    @GetMapping("/recent")
    @ApiMessage("Lấy hoạt động gần đây thành công")
    @Operation(summary = "Get Recent Activities", description = "Get recent activities for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully", content = @Content(schema = @Schema(implementation = RecentActivitiesResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<RecentActivitiesResponse> getRecentActivities(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        String userId = userService.getCurrentUserId();
        RecentActivitiesResponse activities = activityLogService.getRecentActivities(userId, page, size);
        return ResponseEntity.ok(activities);
    }

    // 3. Lấy hoạt động gần đây cho dashboard (không phân trang) - OK
    @GetMapping("/recent-dashboard")
    @ApiMessage("Lấy hoạt động gần đây cho dashboard thành công")
    @Operation(summary = "Get Recent Activities for Dashboard", description = "Get recent activities for dashboard display")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<RecentActivitiesResponse> getRecentActivitiesForDashboard(
            @Parameter(description = "Number of activities to return") @RequestParam(defaultValue = "5") int limit) {
        String userId = userService.getCurrentUserId();
        List<ActivityResponse> activities = activityLogService.getRecentActivitiesForDashboard(userId, limit);
        Long unreadCount = activityLogService.getUnreadCount(userId);

        RecentActivitiesResponse response = RecentActivitiesResponse.builder()
                .activities(activities)
                .totalActivities((long) activities.size())
                .unreadCount(unreadCount)
                .hasMore(false)
                .build();

        return ResponseEntity.ok(response);
    }

    // 4. Đánh dấu tất cả hoạt động là đã đọc - OK
    @PutMapping("/mark-all-read")
    @ApiMessage("Đánh dấu tất cả hoạt động là đã đọc thành công")
    @Operation(summary = "Mark All Activities as Read", description = "Mark all activities as read for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All activities marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<String> markAllActivitiesAsRead() {
        String userId = userService.getCurrentUserId();
        activityLogService.markAllAsRead(userId);
        return ResponseEntity.ok("Tất cả hoạt động đã được đánh dấu là đã đọc");
    }

    // 5. Lấy số hoạt động chưa đọc - OK
    @GetMapping("/unread-count")
    @ApiMessage("Lấy số hoạt động chưa đọc thành công")
    @Operation(summary = "Get Unread Activities Count", description = "Get count of unread activities for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Long> getUnreadCount() {
        String userId = userService.getCurrentUserId();
        Long unreadCount = activityLogService.getUnreadCount(userId);
        return ResponseEntity.ok(unreadCount);
    }

    // 6. Đánh dấu một hoạt động cụ thể là đã đọc - OK
    @PutMapping("/{activityId}/mark-read")
    @ApiMessage("Đánh dấu hoạt động cụ thể là đã đọc thành công")
    @Operation(summary = "Mark Specific Activity as Read", description = "Mark a specific activity as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<Void> markActivityAsRead(
            @Parameter(description = "Activity ID") @PathVariable String activityId) {
        activityLogService.markAsRead(activityId);
        return ResponseEntity.ok().build();
    }
}
