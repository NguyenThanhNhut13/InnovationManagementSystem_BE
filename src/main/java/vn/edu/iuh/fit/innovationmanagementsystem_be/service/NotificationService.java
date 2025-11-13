package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Notification;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserNotification;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.NotificationTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.NotificationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UnreadCountResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.NotificationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserNotificationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public NotificationService(SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            UserNotificationRepository userNotificationRepository,
            DepartmentRepository departmentRepository,
            UserService userService,
            ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void notifyUsersByRole(UserRoleEnum role, String title, String message, NotificationTypeEnum type,
            Map<String, Object> data) {
        try {
            List<User> users = userRepository.findUsersByRole(role);

            if (users.isEmpty()) {
                log.warn("Không tìm thấy user nào có role: {}", role);
                return;
            }

            // Lưu notification vào database
            Notification notification = createNotification(title, message, type, data, null, role.name());

            // Tạo UserNotification cho từng user
            for (User user : users) {
                UserNotification userNotification = new UserNotification();
                userNotification.setUser(user);
                userNotification.setNotification(notification);
                userNotification.setIsRead(false);
                userNotificationRepository.save(userNotification);
            }

            // Gửi WebSocket notification
            Map<String, Object> wsNotification = new HashMap<>();
            wsNotification.put("id", notification.getId());
            wsNotification.put("title", title);
            wsNotification.put("message", message);
            wsNotification.put("type", type);
            wsNotification.put("data", data);
            wsNotification.put("timestamp", System.currentTimeMillis());

            String destination = "/topic/notifications/" + role.name().toLowerCase();

            for (User user : users) {
                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, wsNotification);
                log.info("Đã gửi notification đến user: {} (ID: {})", user.getFullName(), user.getId());
            }

            messagingTemplate.convertAndSend(destination, wsNotification);
            log.info("Đã gửi và lưu notification đến {} users có role: {}", users.size(), role);
        } catch (Exception e) {
            log.error("Lỗi khi gửi notification đến users có role {}: {}", role, e.getMessage(), e);
        }
    }

    @Transactional
    public void notifyUsersByDepartment(String departmentId, String title, String message, NotificationTypeEnum type,
            Map<String, Object> data) {
        try {
            List<User> users = userRepository.findByDepartmentId(departmentId);

            if (users.isEmpty()) {
                log.warn("Không tìm thấy user nào thuộc khoa với ID: {}", departmentId);
                return;
            }

            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + departmentId));

            // Lưu notification vào database
            Notification notification = createNotification(title, message, type, data, department, null);

            // Tạo UserNotification cho từng user
            for (User user : users) {
                UserNotification userNotification = new UserNotification();
                userNotification.setUser(user);
                userNotification.setNotification(notification);
                userNotification.setIsRead(false);
                userNotificationRepository.save(userNotification);
            }

            // Gửi WebSocket notification
            Map<String, Object> wsNotification = new HashMap<>();
            wsNotification.put("id", notification.getId());
            wsNotification.put("title", title);
            wsNotification.put("message", message);
            wsNotification.put("type", type);
            wsNotification.put("data", data);
            wsNotification.put("timestamp", System.currentTimeMillis());

            String destination = "/topic/notifications/department/" + departmentId;

            for (User user : users) {
                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, wsNotification);
                log.info("Đã gửi notification đến user: {} (ID: {})", user.getFullName(), user.getId());
            }

            messagingTemplate.convertAndSend(destination, wsNotification);
            log.info("Đã gửi và lưu notification đến {} users thuộc khoa ID: {}", users.size(), departmentId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi notification đến users thuộc khoa {}: {}", departmentId, e.getMessage(), e);
        }
    }

    private Notification createNotification(String title, String message, NotificationTypeEnum type,
            Map<String, Object> data, Department department, String targetRole) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setDepartment(department);
        notification.setTargetRole(targetRole);

        if (data != null) {
            notification.setReferenceId((String) data.get("roundId"));
            notification.setReferenceType(type.name());
            try {
                notification.setData(objectMapper.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                log.error("Lỗi khi serialize data: {}", e.getMessage());
            }
        }

        return notificationRepository.save(notification);
    }

    public void notifyRoundPublished(String roundId, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roundId", roundId);
        data.put("roundName", roundName);
        data.put("action", "publish");

        String title = "Công bố đợt sáng kiến";
        String message = "Đợt sáng kiến '" + roundName + "' đã được công bố";
        notifyUsersByRole(UserRoleEnum.TRUONG_KHOA, title, message, NotificationTypeEnum.ROUND_PUBLISHED, data);
    }

    public void notifyRoundClosed(String roundId, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roundId", roundId);
        data.put("roundName", roundName);
        data.put("action", "close");

        String title = "Đóng đợt sáng kiến";
        String message = "Đợt sáng kiến '" + roundName + "' đã được đóng";
        notifyUsersByRole(UserRoleEnum.TRUONG_KHOA, title, message, NotificationTypeEnum.ROUND_CLOSED, data);
    }

    public void notifyDepartmentPhasePublished(String departmentId, String departmentName, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("departmentId", departmentId);
        data.put("departmentName", departmentName);
        data.put("roundName", roundName);
        data.put("action", "publish");

        String title = "Công bố giai đoạn khoa";
        String message = "Khoa " + departmentName + " đã công bố giai đoạn cho đợt sáng kiến '" + roundName + "'";
        notifyUsersByDepartment(departmentId, title, message, NotificationTypeEnum.DEPARTMENT_PHASE_PUBLISHED, data);
    }

    public void notifyDepartmentPhaseClosed(String departmentId, String departmentName, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("departmentId", departmentId);
        data.put("departmentName", departmentName);
        data.put("roundName", roundName);
        data.put("action", "close");

        String title = "Đóng giai đoạn khoa";
        String message = "Khoa " + departmentName + " đã đóng giai đoạn cho đợt sáng kiến '" + roundName + "'";
        notifyUsersByDepartment(departmentId, title, message, NotificationTypeEnum.DEPARTMENT_PHASE_CLOSED, data);
    }

    // API: Lấy danh sách notification của user hiện tại
    public ResultPaginationDTO getMyNotifications(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<UserNotification> userNotifications = userNotificationRepository.findByUserIdOrderByCreatedAtDesc(
                currentUser.getId(), pageable);

        Page<NotificationResponse> responsePage = userNotifications.map(this::toNotificationResponse);
        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // API: Lấy danh sách notification chưa đọc
    public ResultPaginationDTO getUnreadNotifications(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<UserNotification> userNotifications = userNotificationRepository.findByUserIdAndIsRead(
                currentUser.getId(), false, pageable);

        Page<NotificationResponse> responsePage = userNotifications.map(this::toNotificationResponse);
        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // API: Đếm số notification chưa đọc
    public UnreadCountResponse getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        Long count = userNotificationRepository.countUnreadByUserId(currentUser.getId());
        return new UnreadCountResponse(count);
    }

    // API: Đánh dấu đã đọc 1 notification
    @Transactional
    public void markAsRead(String notificationId) {
        User currentUser = userService.getCurrentUser();
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(
                currentUser.getId(), notificationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy notification"));

        if (!userNotification.getIsRead()) {
            userNotification.setIsRead(true);
            userNotification.setReadAt(LocalDateTime.now());
            userNotificationRepository.save(userNotification);
        }
    }

    // API: Đánh dấu tất cả đã đọc
    @Transactional
    public void markAllAsRead() {
        User currentUser = userService.getCurrentUser();
        userNotificationRepository.markAllAsReadByUserId(currentUser.getId());
    }

    // API: Xóa notification
    @Transactional
    public void deleteNotification(String notificationId) {
        User currentUser = userService.getCurrentUser();
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(
                currentUser.getId(), notificationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy notification"));

        userNotificationRepository.delete(userNotification);
    }

    private NotificationResponse toNotificationResponse(UserNotification userNotification) {
        Notification notification = userNotification.getNotification();
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setReferenceId(notification.getReferenceId());
        response.setReferenceType(notification.getReferenceType());
        response.setTargetRole(notification.getTargetRole());
        response.setCreatedAt(notification.getCreatedAt());
        response.setIsRead(userNotification.getIsRead());
        response.setReadAt(userNotification.getReadAt());

        if (notification.getDepartment() != null) {
            response.setDepartmentId(notification.getDepartment().getId());
            response.setDepartmentName(notification.getDepartment().getDepartmentName());
        }

        if (notification.getData() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = objectMapper.readValue(notification.getData(), Map.class);
                response.setData(dataMap);
            } catch (JsonProcessingException e) {
                log.error("Lỗi khi deserialize data: {}", e.getMessage());
            }
        }

        return response;
    }
}
