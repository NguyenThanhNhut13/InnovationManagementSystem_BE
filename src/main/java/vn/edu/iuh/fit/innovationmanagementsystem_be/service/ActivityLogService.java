package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ActivityLog;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.ActivityResponseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ActivityResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RecentActivitiesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ActivityLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityResponseMapper activityResponseMapper;

    // 1. Lấy hoạt động gần đây với phân trang - OK
    @Transactional(readOnly = true)
    public RecentActivitiesResponse getRecentActivities(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> activityLogPage = activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<ActivityResponse> activities = activityLogPage.getContent().stream()
                .map(activityResponseMapper::toActivityResponse)
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

    // 2. Lấy hoạt động gần đây cho dashboard (không phân trang) - OK
    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentActivitiesForDashboard(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<ActivityLog> activityLogPage = activityLogRepository.findRecentActivitiesForDashboard(userId, pageable);

        return activityLogPage.getContent().stream()
                .map(activityResponseMapper::toActivityResponse)
                .collect(Collectors.toList());
    }

    // 3. Đánh dấu tất cả hoạt động là đã đọc - OK
    public void markAllAsRead(String userId) {
        activityLogRepository.markAllAsReadByUserId(userId);
    }

    // 4. Lấy số hoạt động chưa đọc - OK
    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        return activityLogRepository.countByUserIdAndIsReadFalse(userId);
    }

    // 5. Đánh dấu một hoạt động cụ thể là đã đọc - OK
    public void markAsRead(String activityId) {
        activityLogRepository.findById(activityId).ifPresent(activity -> {
            activity.setIsRead(true);
            activityLogRepository.save(activity);
        });
    }

    // 6. Tạo activity log mới
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

}
