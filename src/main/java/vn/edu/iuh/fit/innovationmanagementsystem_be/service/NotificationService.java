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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.NotificationDetailResponse;
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
    public void notifyUsersByRole(UserRoleEnum role,
            String actorId,
            String actorFullName,
            String selfTitle,
            String othersTitle,
            String selfMessage,
            String othersMessage,
            NotificationTypeEnum type,
            Map<String, Object> data) {
        try {
            List<User> users = userRepository.findUsersByRole(role);

            if (users.isEmpty()) {
                throw new IdInvalidException("Không tìm thấy user nào có role: " + role);
            }

            String resolvedActorName = actorFullName != null ? actorFullName : "Người phụ trách";

            for (User user : users) {
                boolean isActor = actorId != null && actorId.equals(user.getId());
                String finalTitle = isActor ? selfTitle : othersTitle;
                String finalMessage = isActor ? selfMessage : othersMessage;

                Map<String, Object> personalizedData = data != null ? new HashMap<>(data) : new HashMap<>();
                personalizedData.put("actorId", actorId);
                personalizedData.put("actorName", resolvedActorName);
                personalizedData.put("isActor", isActor);

                Notification notification = createNotification(finalTitle, finalMessage, type, personalizedData, null,
                        role.name());

                Map<String, Object> wsNotification = createWebSocketNotification(
                        notification.getId(), finalTitle, finalMessage, type, personalizedData);

                UserNotification userNotification = new UserNotification();
                userNotification.setUser(user);
                userNotification.setNotification(notification);
                userNotification.setIsRead(false);
                userNotificationRepository.save(userNotification);

                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, wsNotification);
            }

            log.info("Đã gửi và lưu notification đến {} users có role: {}", users.size(), role);
        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi gửi notification đến users có role " + role);
        }
    }

    @Transactional
    public void notifyUsersByDepartment(String departmentId, String title, String message, NotificationTypeEnum type,
            Map<String, Object> data) {
        try {
            List<User> users = userRepository.findByDepartmentId(departmentId);

            if (users.isEmpty()) {
                throw new IdInvalidException("Không tìm thấy user nào thuộc khoa với ID: " + departmentId);

            }

            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + departmentId));

            // Lưu notification vào database (lưu đầy đủ data)
            Notification notification = createNotification(title, message, type, data, department, null);

            // Gửi WebSocket notification (gộp tất cả vào data)
            Map<String, Object> wsNotification = createWebSocketNotification(
                    notification.getId(), title, message, type, data);

            for (User user : users) {
                // Tạo UserNotification cho user
                UserNotification userNotification = new UserNotification();
                userNotification.setUser(user);
                userNotification.setNotification(notification);
                userNotification.setIsRead(false);
                userNotificationRepository.save(userNotification);

                // Gửi WebSocket notification đến queue riêng của user
                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, wsNotification);
            }

            log.info("Đã gửi và lưu notification đến {} users thuộc khoa: {}",
                    users.size(), department.getDepartmentName());
        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi gửi notification đến users thuộc khoa :" + departmentId);
        }
    }

    @Transactional
    public void notifyUsersByDepartmentAndRoles(String departmentId, List<UserRoleEnum> roles, String title,
            String message, NotificationTypeEnum type, Map<String, Object> data) {
        try {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy khoa với ID: " + departmentId));

            // Lấy users thuộc department và có một trong các roles
            List<User> users = userRepository.findByDepartmentIdAndRoles(departmentId, roles);

            if (users.isEmpty()) {
                log.warn("Không tìm thấy user nào thuộc khoa {} với roles: {}",
                        department.getDepartmentName(), roles);
                return;
            }

            // Lưu notification vào database
            String targetRoles = roles.stream()
                    .map(UserRoleEnum::name)
                    .collect(java.util.stream.Collectors.joining(","));
            Notification notification = createNotification(title, message, type, data, department, targetRoles);

            // Gửi WebSocket notification
            Map<String, Object> wsNotification = createWebSocketNotification(
                    notification.getId(), title, message, type, data);

            for (User user : users) {
                // Tạo UserNotification cho user
                UserNotification userNotification = new UserNotification();
                userNotification.setUser(user);
                userNotification.setNotification(notification);
                userNotification.setIsRead(false);
                userNotificationRepository.save(userNotification);

                // Gửi WebSocket notification đến queue riêng của user
                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, wsNotification);
            }

            log.info("Đã gửi và lưu notification đến {} users thuộc khoa {} với roles: {}",
                    users.size(), department.getDepartmentName(), roles);
        } catch (Exception e) {
            log.error("Lỗi khi gửi notification đến users thuộc khoa {} với roles {}: {}",
                    departmentId, roles, e.getMessage());
        }
    }

    // Helper method: Tạo WebSocket notification data (chỉ gửi data cần thiết)
    private Map<String, Object> createWebSocketData(Map<String, Object> fullData) {
        Map<String, Object> wsData = new HashMap<>();
        if (fullData != null) {
            // Chỉ gửi các trường cần thiết, không gửi url và roundName
            if (fullData.containsKey("roundId")) {
                wsData.put("roundId", fullData.get("roundId"));
            }
            if (fullData.containsKey("action")) {
                wsData.put("action", fullData.get("action"));
            }
            if (fullData.containsKey("departmentId")) {
                wsData.put("departmentId", fullData.get("departmentId"));
            }
        }
        return wsData;
    }

    // Helper method: Tạo WebSocket notification (gộp tất cả vào data)
    private Map<String, Object> createWebSocketNotification(String notificationId, String title, String message,
            NotificationTypeEnum type, Map<String, Object> fullData) {
        Map<String, Object> wsData = createWebSocketData(fullData);
        wsData.put("id", notificationId);
        wsData.put("title", title);
        wsData.put("message", message);
        wsData.put("type", type);
        wsData.put("timestamp", System.currentTimeMillis());

        Map<String, Object> wsNotification = new HashMap<>();
        wsNotification.put("data", wsData);
        return wsNotification;
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
                throw new IdInvalidException("Lỗi khi serialize data: {}" + e.getMessage());
            }
        }

        return notificationRepository.save(notification);
    }

    public void notifyRoundPublished(String roundId, String roundName, String actorId, String actorFullName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roundId", roundId);
        data.put("roundName", roundName);
        data.put("action", "publish");
        data.put("url", "/innovation-rounds/" + roundId);

        String actorDisplayName = actorFullName != null ? actorFullName : "Người phụ trách";

        String selfTitle = "Bạn đã công bố đợt sáng kiến mới";
        String othersTitle = actorDisplayName + " đã công bố đợt sáng kiến mới";

        String selfMessage = "Bạn vừa công bố đợt sáng kiến \"" + roundName
                + "\" thành công. ";
        String othersMessage = actorDisplayName + " vừa công bố đợt sáng kiến \"" + roundName
                + "\". Vui lòng kiểm tra và chuẩn bị các giai đoạn cho khoa của bạn.";

        boolean actorIsQlkhHtqt = isUserInRole(actorId, UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT);

        if (actorIsQlkhHtqt) {
            notifyDepartmentManagersToConfigureRound(
                    roundId,
                    roundName,
                    actorId,
                    actorDisplayName,
                    data);
        } else {
            notifyUsersByRole(
                    UserRoleEnum.TRUONG_KHOA,
                    actorId,
                    actorDisplayName,
                    selfTitle,
                    othersTitle,
                    selfMessage,
                    othersMessage,
                    NotificationTypeEnum.ROUND_PUBLISHED,
                    data);
        }

        notifyUsersByRole(
                UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT,
                actorId,
                actorDisplayName,
                selfTitle,
                othersTitle,
                selfMessage,
                othersMessage,
                NotificationTypeEnum.ROUND_PUBLISHED,
                data);
    }

    public void notifyRoundClosed(String roundId, String roundName, String actorId, String actorFullName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roundId", roundId);
        data.put("roundName", roundName);
        data.put("action", "close");
        data.put("url", "/innovation-rounds/" + roundId);

        String actorDisplayName = actorFullName != null ? actorFullName : "Người phụ trách";

        String selfTitle = "Bạn đã đóng đợt sáng kiến";
        String othersTitle = actorDisplayName + " đã đóng đợt sáng kiến";

        String selfMessage = "Bạn vừa đóng đợt sáng kiến \"" + roundName
                + "\". Hệ thống sẽ ngừng nhận thêm chỉnh sửa cho đợt này.";
        String othersMessage = actorDisplayName + " vừa đóng đợt sáng kiến \"" + roundName
                + "\". Không thể thực hiện thêm thay đổi nào cho đợt này.";

        boolean actorIsQlkhHtqt = isUserInRole(actorId, UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT);

        if (actorIsQlkhHtqt) {
            notifyDepartmentManagersRoundClosed(
                    roundId,
                    roundName,
                    actorId,
                    actorDisplayName,
                    data);
        } else {
            notifyUsersByRole(
                    UserRoleEnum.TRUONG_KHOA,
                    actorId,
                    actorDisplayName,
                    selfTitle,
                    othersTitle,
                    selfMessage,
                    othersMessage,
                    NotificationTypeEnum.ROUND_CLOSED,
                    data);
        }

        notifyUsersByRole(
                UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT,
                actorId,
                actorDisplayName,
                selfTitle,
                othersTitle,
                selfMessage,
                othersMessage,
                NotificationTypeEnum.ROUND_CLOSED,
                data);
    }

    public void notifyDepartmentPhasePublished(String departmentId, String departmentName, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("departmentId", departmentId);
        data.put("departmentName", departmentName);
        data.put("roundName", roundName);
        data.put("action", "publish");
        data.put("url", "/department-phases?departmentId=" + departmentId);

        String title = "Công bố giai đoạn khoa";
        String message = "Khoa " + departmentName + " đã công bố các giai đoạn cho đợt sáng kiến \"" + roundName
                + "\". " +
                "Vui lòng xem chi tiết và chuẩn bị nộp hồ sơ sáng kiến.";

        // Gửi thông báo cho tất cả users trong department
        notifyUsersByDepartment(departmentId, title, message, NotificationTypeEnum.DEPARTMENT_PHASE_PUBLISHED, data);

        // Gửi thông báo bổ sung cho TRUONG_KHOA và QUAN_TRI_VIEN_KHOA (nếu họ chưa nhận
        // được)
        // Thông báo này có nội dung khác để phân biệt với thông báo chung
        String titleForManagers = "Công bố giai đoạn khoa - Quản lý";
        String messageForManagers = "Khoa " + departmentName + " đã công bố các giai đoạn cho đợt sáng kiến \""
                + roundName
                + "\". " +
                "Vui lòng theo dõi và quản lý quá trình nộp hồ sơ sáng kiến của giảng viên.";
        notifyUsersByDepartmentAndRoles(departmentId,
                List.of(UserRoleEnum.TRUONG_KHOA, UserRoleEnum.QUAN_TRI_VIEN_KHOA),
                titleForManagers, messageForManagers, NotificationTypeEnum.DEPARTMENT_PHASE_PUBLISHED, data);
    }

    public void notifyDepartmentPhaseClosed(String departmentId, String departmentName, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("departmentId", departmentId);
        data.put("departmentName", departmentName);
        data.put("roundName", roundName);
        data.put("action", "close");
        data.put("url", "/department-phases?departmentId=" + departmentId);

        String title = "Đóng giai đoạn khoa";
        String message = "Khoa " + departmentName + " đã đóng các giai đoạn cho đợt sáng kiến \"" + roundName + "\". " +
                "Không thể nộp hoặc chỉnh sửa hồ sơ sáng kiến nữa.";

        // Gửi thông báo cho tất cả users trong department
        notifyUsersByDepartment(departmentId, title, message, NotificationTypeEnum.DEPARTMENT_PHASE_CLOSED, data);

        // Gửi thông báo bổ sung cho TRUONG_KHOA và QUAN_TRI_VIEN_KHOA (nếu họ chưa nhận
        // được)
        // Thông báo này có nội dung khác để phân biệt với thông báo chung
        String titleForManagers = "Đóng giai đoạn khoa - Quản lý";
        String messageForManagers = "Khoa " + departmentName + " đã đóng các giai đoạn cho đợt sáng kiến \"" + roundName
                + "\". " +
                "Vui lòng kiểm tra và tổng hợp kết quả nộp hồ sơ sáng kiến.";
        notifyUsersByDepartmentAndRoles(departmentId,
                List.of(UserRoleEnum.TRUONG_KHOA, UserRoleEnum.QUAN_TRI_VIEN_KHOA),
                titleForManagers, messageForManagers, NotificationTypeEnum.DEPARTMENT_PHASE_CLOSED, data);
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

        // Sử dụng NotificationDetailResponse với format đẹp
        NotificationDetailResponse detailResponse = new NotificationDetailResponse();

        detailResponse.setId(notification.getId());
        detailResponse.setTitle(notification.getTitle());
        detailResponse.setMessage(notification.getMessage());
        detailResponse.setType(notification.getType());
        detailResponse.setReferenceId(notification.getReferenceId());
        detailResponse.setReferenceType(notification.getReferenceType());
        detailResponse.setTargetRole(notification.getTargetRole());
        detailResponse.setCreatedAt(notification.getCreatedAt());
        detailResponse.setIsRead(userNotification.getIsRead());
        detailResponse.setReadAt(userNotification.getReadAt());

        if (notification.getDepartment() != null) {
            detailResponse.setDepartmentId(notification.getDepartment().getId());
            detailResponse.setDepartmentName(notification.getDepartment().getDepartmentName());
        }

        if (notification.getData() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = objectMapper.readValue(notification.getData(), Map.class);
                detailResponse.setData(dataMap);
            } catch (JsonProcessingException e) {
                throw new IdInvalidException("Lỗi khi deserialize data: " + e.getMessage());
            }
        }

        // Cast to NotificationResponse để return
        NotificationResponse response = new NotificationResponse();
        response.setId(detailResponse.getId());
        response.setTitle(detailResponse.getTitle());
        response.setMessage(detailResponse.getMessage());
        response.setType(detailResponse.getType());
        response.setReferenceId(detailResponse.getReferenceId());
        response.setReferenceType(detailResponse.getReferenceType());
        response.setDepartmentId(detailResponse.getDepartmentId());
        response.setDepartmentName(detailResponse.getDepartmentName());
        response.setTargetRole(detailResponse.getTargetRole());
        response.setCreatedAt(detailResponse.getCreatedAt());
        response.setIsRead(detailResponse.getIsRead());
        response.setReadAt(detailResponse.getReadAt());

        // Thêm formatted dates vào data
        if (response.getData() == null) {
            response.setData(new HashMap<>());
        }
        response.getData().put("createdAtFormatted", detailResponse.getCreatedAtFormatted());
        response.getData().put("createdDate", detailResponse.getCreatedDate());
        response.getData().put("createdTime", detailResponse.getCreatedTime());
        response.getData().put("createdDateTimeFull", detailResponse.getCreatedDateTimeFull());
        if (detailResponse.getReadAtFormatted() != null) {
            response.getData().put("readAtFormatted", detailResponse.getReadAtFormatted());
        }

        return response;
    }

    private void notifyDepartmentManagersToConfigureRound(String roundId,
            String roundName,
            String actorId,
            String actorDisplayName,
            Map<String, Object> baseData) {

        Map<String, Object> managerData = baseData != null ? new HashMap<>(baseData) : new HashMap<>();
        managerData.put("task", "configure_department_phases");

        String title = "Đợt sáng kiến đã mở";
        String message = "Đợt sáng kiến \"" + roundName + "\" đã được " + actorDisplayName
                + " công bố. Vui lòng cấu hình thời gian cho khoa của bạn.";

        List<UserRoleEnum> targetRoles = List.of(
                UserRoleEnum.TRUONG_KHOA,
                UserRoleEnum.QUAN_TRI_VIEN_KHOA);

        for (UserRoleEnum role : targetRoles) {
            notifyUsersByRole(
                    role,
                    actorId,
                    actorDisplayName,
                    title,
                    title,
                    message,
                    message,
                    NotificationTypeEnum.ROUND_PUBLISHED,
                    managerData);
        }
    }

    private boolean isUserInRole(String userId, UserRoleEnum role) {
        if (userId == null || role == null) {
            return false;
        }
        return userRepository.userHasRole(userId, role);
    }

    private void notifyDepartmentManagersRoundClosed(String roundId,
            String roundName,
            String actorId,
            String actorDisplayName,
            Map<String, Object> baseData) {

        Map<String, Object> managerData = baseData != null ? new HashMap<>(baseData) : new HashMap<>();
        managerData.put("task", "finalize_department_phases");

        String title = "Đợt sáng kiến đã đóng";
        String message = "Đợt sáng kiến \"" + roundName + "\" đã được " + actorDisplayName
                + " đóng lại. Vui lòng rà soát và hoàn tất báo cáo cho khoa của bạn.";

        List<UserRoleEnum> targetRoles = List.of(
                UserRoleEnum.TRUONG_KHOA,
                UserRoleEnum.QUAN_TRI_VIEN_KHOA);

        for (UserRoleEnum role : targetRoles) {
            notifyUsersByRole(
                    role,
                    actorId,
                    actorDisplayName,
                    title,
                    title,
                    message,
                    message,
                    NotificationTypeEnum.ROUND_CLOSED,
                    managerData);
        }
    }
}
