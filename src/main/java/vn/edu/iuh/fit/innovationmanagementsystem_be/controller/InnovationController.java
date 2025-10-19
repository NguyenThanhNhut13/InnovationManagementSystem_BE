package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationAcademicYearStatisticsDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UpcomingDeadlinesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RecentActivitiesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ActivityResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.InnovationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ActivityLogService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ActivityLogRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Innovation Management", description = "Innovation management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class InnovationController {

        private final InnovationService innovationService;
        private final ActivityLogService activityLogService;
        private final UserService userService;

        public InnovationController(InnovationService innovationService, ActivityLogService activityLogService,
                        UserService userService, ActivityLogRepository activityLogRepository) {
                this.innovationService = innovationService;
                this.activityLogService = activityLogService;
                this.userService = userService;

        }

        // 1. Lấy danh sách sáng kiến
        @GetMapping("/innovations")
        @ApiMessage("Lấy danh sách sáng kiến thành công")
        @Operation(summary = "Get All Innovations", description = "Get paginated list of all innovations with filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovations retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ResultPaginationDTO> getAllInnovations(
                        @Parameter(description = "Filter specification for innovations") @Filter Specification<Innovation> specification,
                        @Parameter(description = "Pagination parameters") Pageable pageable) {
                return ResponseEntity.ok(innovationService.getAllInnovations(specification, pageable));
        }

        // 2. Lấy sáng kiến by Id
        @GetMapping("/innovations/{id}")
        @ApiMessage("Lấy thông tin sáng kiến bằng id thành công")
        @Operation(summary = "Get Innovation by ID", description = "Get innovation details by innovation ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Innovation not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<InnovationResponse> getInnovationById(
                        @Parameter(description = "Innovation ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(innovationService.getInnovationById(id));
        }

        // 3. Tạo sáng kiến & Submit Form Data (Tạo sáng kiến tự động khi điền form)
        @PostMapping("/innovations/form-data")
        @ApiMessage("Tạo sáng kiến và điền thông tin thành công")
        @Operation(summary = "Create Innovation with Form Data", description = "Create a new innovation and submit form data")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation created and form data submitted successfully", content = @Content(schema = @Schema(implementation = InnovationFormDataResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<InnovationFormDataResponse> createInnovationAndSubmitFormData(
                        @Parameter(description = "Innovation form data request", required = true) @Valid @RequestBody InnovationFormDataRequest request) {
                InnovationFormDataResponse response = innovationService.createInnovationAndSubmitFormData(request);
                return ResponseEntity.ok(response);
        }

        // 4. Cập nhật FormData sáng kiến
        @PutMapping("/innovations/{innovationId}/form-data")
        @ApiMessage("Cập nhật thông tin form thành công")
        public ResponseEntity<InnovationFormDataResponse> updateInnovationFormData(
                        @PathVariable String innovationId,
                        @Valid @RequestBody InnovationFormDataRequest request) {
                InnovationFormDataResponse response = innovationService.updateInnovationFormData(innovationId, request);
                return ResponseEntity.ok(response);
        }

        // 5. Lấy FormData sáng kiến
        @GetMapping("/innovations/{innovationId}/form-data")
        @ApiMessage("Lấy FormData của sáng kiến thành công")
        public ResponseEntity<InnovationFormDataResponse> getInnovationFormData(
                        @PathVariable String innovationId,
                        @RequestParam(required = false) String templateId) {
                InnovationFormDataResponse response = innovationService.getInnovationFormData(innovationId, templateId);
                return ResponseEntity.ok(response);
        }

        // 6. Lấy danh sách sáng kiến của tôi theo trạng thái
        @GetMapping("/innovations/my-innovations")
        @ApiMessage("Lấy danh sách sáng kiến của tôi theo trạng thái thành công")
        public ResponseEntity<ResultPaginationDTO> getMyInnovationsByStatus(
                        @RequestParam String status,
                        Pageable pageable) {
                return ResponseEntity.ok(innovationService.getInnovationsByUserAndStatus(status, pageable));
        }

        // 7. Lấy thống kê sáng kiến của giảng viên
        @GetMapping("/innovations/statistics")
        @ApiMessage("Lấy thống kê sáng kiến thành công")
        @Operation(summary = "Get Innovation Statistics", description = "Get innovation statistics for current user (GIANG_VIEN role)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationStatisticsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - GIANG_VIEN role required")
        })
        public ResponseEntity<InnovationStatisticsDTO> getInnovationStatistics() {
                InnovationStatisticsDTO statistics = innovationService.getInnovationStatisticsForCurrentUser();
                return ResponseEntity.ok(statistics);
        }

        // 8. Lấy thống kê sáng kiến theo năm học
        @GetMapping("/innovations/statistics/academic-year")
        @ApiMessage("Lấy thống kê sáng kiến theo năm học thành công")
        @Operation(summary = "Get Innovation Statistics by Academic Year", description = "Get innovation statistics grouped by academic year for current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Academic year statistics retrieved successfully", content = @Content(schema = @Schema(implementation = InnovationAcademicYearStatisticsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - GIANG_VIEN role required")
        })
        public ResponseEntity<InnovationAcademicYearStatisticsDTO> getInnovationStatisticsByAcademicYear() {
                InnovationAcademicYearStatisticsDTO statistics = innovationService
                                .getInnovationStatisticsByAcademicYearForCurrentUser();
                return ResponseEntity.ok(statistics);
        }

        // 9. Lấy hạn chót sắp tới
        @GetMapping("/innovations/upcoming-deadlines")
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

        // 10. Lấy hoạt động gần đây
        @GetMapping("/innovations/activities/recent")
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

        // 11. Lấy hoạt động gần đây cho dashboard (không phân trang)
        @GetMapping("/innovations/activities/recent-dashboard")
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

        // 12. Đánh dấu tất cả hoạt động là đã đọc
        @PutMapping("/innovations/activities/mark-all-read")
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

        // 13. Lấy số hoạt động chưa đọc
        @GetMapping("/innovations/activities/unread-count")
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

        // 15. Đánh dấu một hoạt động cụ thể là đã đọc
        @PutMapping("/innovations/activities/{activityId}/mark-read")
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

        // 16. Duyệt sáng kiến
        @PutMapping("/innovations/{innovationId}/approve")
        @ApiMessage("Duyệt sáng kiến thành công")
        @Operation(summary = "Approve Innovation", description = "Approve an innovation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation approved successfully", content = @Content(schema = @Schema(implementation = InnovationResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Innovation not found")
        })
        public ResponseEntity<InnovationResponse> approveInnovation(
                        @Parameter(description = "Innovation ID") @PathVariable String innovationId,
                        @Parameter(description = "Approval reason") @RequestParam(required = false) String reason) {
                InnovationResponse response = innovationService.approveInnovation(innovationId, reason);
                return ResponseEntity.ok(response);
        }

        // 17. Từ chối sáng kiến
        @PutMapping("/innovations/{innovationId}/reject")
        @ApiMessage("Từ chối sáng kiến thành công")
        @Operation(summary = "Reject Innovation", description = "Reject an innovation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Innovation rejected successfully", content = @Content(schema = @Schema(implementation = InnovationResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Innovation not found")
        })
        public ResponseEntity<InnovationResponse> rejectInnovation(
                        @Parameter(description = "Innovation ID") @PathVariable String innovationId,
                        @Parameter(description = "Rejection reason") @RequestParam(required = false) String reason) {
                InnovationResponse response = innovationService.rejectInnovation(innovationId, reason);
                return ResponseEntity.ok(response);
        }

}
