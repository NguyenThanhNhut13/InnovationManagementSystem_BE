package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ActivityLog;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.ActivityIconMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.ActivityColorMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ActivityResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RecentActivitiesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ActivityLogRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    // 1. Tạo activity log mới
    public ActivityLog createActivityLog(String userId, String innovationId, String innovationName,
            InnovationStatusEnum activityType, String message) {
        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .innovationId(innovationId)
                .innovationName(innovationName)
                .activityType(activityType)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return activityLogRepository.save(activityLog);
    }

    // 2. Lấy hoạt động gần đây với phân trang
    @Transactional(readOnly = true)
    public RecentActivitiesResponse getRecentActivities(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> activityLogPage = activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<ActivityResponse> activities = activityLogPage.getContent().stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());

        Long unreadCount = activityLogRepository.countByUserIdAndIsReadFalse(userId);

        return RecentActivitiesResponse.builder()
                .activities(activities)
                .totalActivities(activityLogPage.getTotalElements())
                .unreadCount(unreadCount)
                .hasMore(activityLogPage.hasNext())
                .currentPage(page)
                .totalPages(activityLogPage.getTotalPages())
                .build();
    }

    // 3. Lấy hoạt động gần đây cho dashboard (không phân trang)
    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentActivitiesForDashboard(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<ActivityLog> activityLogPage = activityLogRepository.findRecentActivitiesForDashboard(userId, pageable);

        return activityLogPage.getContent().stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());
    }

    // 4. Lấy số hoạt động chưa đọc
    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        return activityLogRepository.countByUserIdAndIsReadFalse(userId);
    }

    // 5. Đánh dấu tất cả hoạt động là đã đọc
    public void markAllAsRead(String userId) {
        activityLogRepository.markAllAsReadByUserId(userId);
    }

    // 6. Đánh dấu một hoạt động cụ thể là đã đọc
    public void markAsRead(String activityId) {
        activityLogRepository.findById(activityId).ifPresent(activity -> {
            activity.setIsRead(true);
            activityLogRepository.save(activity);
        });
    }

    /**
     * Map ActivityLog entity sang ActivityResponse DTO
     */
    private ActivityResponse mapToActivityResponse(ActivityLog activityLog) {
        return ActivityResponse.builder()
                .id(activityLog.getId())
                .innovationId(activityLog.getInnovationId())
                .innovationName(activityLog.getInnovationName())
                .activityType(activityLog.getActivityType())
                .message(activityLog.getMessage())
                .isRead(activityLog.getIsRead())
                .createdAt(activityLog.getCreatedAt())
                .timeAgo(calculateTimeAgo(activityLog.getCreatedAt()))
                .iconType(getIconType(activityLog.getActivityType()))
                .iconColor(getIconColor(activityLog.getActivityType()))
                .build();
    }

    /**
     * Tính thời gian đã trôi qua
     */
    private String calculateTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();

        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    // 7. Lấy loại icon dựa trên activity type
    private String getIconType(InnovationStatusEnum activityType) {
        return ActivityIconMapper.getIconType(activityType);
    }

    // 8. Lấy màu icon dựa trên activity type
    private String getIconColor(InnovationStatusEnum activityType) {
        return ActivityColorMapper.getIconColor(activityType);
    }
}
