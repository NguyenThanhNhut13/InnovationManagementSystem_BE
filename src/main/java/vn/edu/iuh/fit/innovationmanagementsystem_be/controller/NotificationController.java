package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UnreadCountResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.NotificationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "Notification management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    @ApiMessage("Lấy danh sách thông báo của tôi thành công")
    @Operation(summary = "Get My Notifications", description = "Get paginated list of current user's notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getMyNotifications(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyNotifications(pageable));
    }

    @GetMapping("/unread")
    @ApiMessage("Lấy danh sách thông báo chưa đọc thành công")
    @Operation(summary = "Get Unread Notifications", description = "Get paginated list of unread notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getUnreadNotifications(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(pageable));
    }

    @GetMapping("/unread-count")
    @ApiMessage("Lấy số lượng thông báo chưa đọc thành công")
    @Operation(summary = "Get Unread Count", description = "Get count of unread notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully", content = @Content(schema = @Schema(implementation = UnreadCountResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PutMapping("/{notificationId}/read")
    @ApiMessage("Đánh dấu thông báo đã đọc thành công")
    @Operation(summary = "Mark Notification as Read", description = "Mark a notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @ApiMessage("Đánh dấu tất cả thông báo đã đọc thành công")
    @Operation(summary = "Mark All as Read", description = "Mark all notifications as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications marked as read successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    @ApiMessage("Xóa thông báo thành công")
    @Operation(summary = "Delete Notification", description = "Delete a notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }
}
