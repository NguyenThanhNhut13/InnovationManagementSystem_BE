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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Notification;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserNotification;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.NotificationTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.NotificationDetailResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.NotificationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UnreadCountResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.NotificationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserNotificationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

        private static final List<UserRoleEnum> DEPARTMENT_MANAGER_ROLES = List.of(
                        UserRoleEnum.TRUONG_KHOA,
                        UserRoleEnum.QUAN_TRI_VIEN_KHOA);
        private static final String MANAGER_TARGET_ROLE = "TRUONG_KHOA,QUAN_TRI_VIEN_KHOA";

        private final SimpMessagingTemplate messagingTemplate;
        private final UserRepository userRepository;
        private final NotificationRepository notificationRepository;
        private final UserNotificationRepository userNotificationRepository;
        private final DepartmentRepository departmentRepository;
        private final DepartmentPhaseRepository departmentPhaseRepository;
        private final UserService userService;
        private final ObjectMapper objectMapper;

        public NotificationService(SimpMessagingTemplate messagingTemplate,
                        UserRepository userRepository,
                        NotificationRepository notificationRepository,
                        UserNotificationRepository userNotificationRepository,
                        DepartmentRepository departmentRepository,
                        DepartmentPhaseRepository departmentPhaseRepository,
                        UserService userService,
                        ObjectMapper objectMapper) {
                this.messagingTemplate = messagingTemplate;
                this.userRepository = userRepository;
                this.notificationRepository = notificationRepository;
                this.userNotificationRepository = userNotificationRepository;
                this.departmentRepository = departmentRepository;
                this.departmentPhaseRepository = departmentPhaseRepository;
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

                                Map<String, Object> personalizedData = data != null ? new HashMap<>(data)
                                                : new HashMap<>();
                                personalizedData.put("actorId", actorId);
                                personalizedData.put("actorName", resolvedActorName);
                                personalizedData.put("isActor", isActor);

                                Notification notification = createNotification(finalTitle, finalMessage, type,
                                                personalizedData, null,
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
        public void notifyUsersByDepartment(String departmentId, String title, String message,
                        NotificationTypeEnum type,
                        Map<String, Object> data) {
                try {
                        List<User> users = userRepository.findByDepartmentId(departmentId);

                        if (users.isEmpty()) {
                                throw new IdInvalidException(
                                                "Không tìm thấy user nào thuộc khoa với ID: " + departmentId);

                        }

                        Department department = departmentRepository.findById(departmentId)
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy khoa với ID: " + departmentId));

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
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy khoa với ID: " + departmentId));

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
                        Notification notification = createNotification(title, message, type, data, department,
                                        targetRoles);

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

        public void notifyDepartmentPhasePublished(String departmentId,
                        String departmentName,
                        String roundName,
                        String actorId,
                        String actorFullName) {

                Department department = departmentRepository.findById(departmentId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy khoa với ID: " + departmentId));

                String resolvedDepartmentName = departmentName != null ? departmentName
                                : department.getDepartmentName();
                String actorDisplayName = actorFullName != null ? actorFullName : "Người phụ trách";

                Map<String, Object> baseData = buildDepartmentPhaseData(
                                departmentId,
                                resolvedDepartmentName,
                                roundName,
                                "publish");

                List<User> departmentManagers = userRepository.findByDepartmentIdAndRoles(
                                departmentId,
                                DEPARTMENT_MANAGER_ROLES);

                notifyDepartmentManagersPhaseAction(
                                department,
                                departmentManagers,
                                actorId,
                                actorDisplayName,
                                roundName,
                                DepartmentPhaseAction.PUBLISH,
                                baseData,
                                NotificationTypeEnum.DEPARTMENT_PHASE_PUBLISHED);

                notifyDepartmentMembersAboutPhaseAction(
                                department,
                                departmentManagers,
                                actorDisplayName,
                                roundName,
                                DepartmentPhaseAction.PUBLISH,
                                baseData,
                                NotificationTypeEnum.DEPARTMENT_PHASE_PUBLISHED);
        }

        public void notifyDepartmentPhaseClosed(String departmentId,
                        String departmentName,
                        String roundName,
                        String actorId,
                        String actorFullName) {

                Department department = departmentRepository.findById(departmentId)
                                .orElseThrow(() -> new IdInvalidException(
                                                "Không tìm thấy khoa với ID: " + departmentId));

                String resolvedDepartmentName = departmentName != null ? departmentName
                                : department.getDepartmentName();
                String actorDisplayName = actorFullName != null ? actorFullName : "Người phụ trách";

                Map<String, Object> baseData = buildDepartmentPhaseData(
                                departmentId,
                                resolvedDepartmentName,
                                roundName,
                                "close");

                List<User> departmentManagers = userRepository.findByDepartmentIdAndRoles(
                                departmentId,
                                DEPARTMENT_MANAGER_ROLES);

                notifyDepartmentManagersPhaseAction(
                                department,
                                departmentManagers,
                                actorId,
                                actorDisplayName,
                                roundName,
                                DepartmentPhaseAction.CLOSE,
                                baseData,
                                NotificationTypeEnum.DEPARTMENT_PHASE_CLOSED);

                notifyDepartmentMembersAboutPhaseAction(
                                department,
                                departmentManagers,
                                actorDisplayName,
                                roundName,
                                DepartmentPhaseAction.CLOSE,
                                baseData,
                                NotificationTypeEnum.DEPARTMENT_PHASE_CLOSED);
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

        private Map<String, Object> buildDepartmentPhaseData(String departmentId,
                        String departmentName,
                        String roundName,
                        String action) {

                Map<String, Object> data = new HashMap<>();
                data.put("departmentId", departmentId);
                data.put("departmentName", departmentName);
                data.put("roundName", roundName);
                data.put("action", action);
                data.put("url", "/department-phases?departmentId=" + departmentId);
                return data;
        }

        private void notifyDepartmentManagersPhaseAction(Department department,
                        List<User> departmentManagers,
                        String actorId,
                        String actorDisplayName,
                        String roundName,
                        DepartmentPhaseAction action,
                        Map<String, Object> baseData,
                        NotificationTypeEnum type) {

                if (departmentManagers == null || departmentManagers.isEmpty()) {
                        log.warn("Không tìm thấy quản lý thuộc khoa {}", department.getDepartmentName());
                        return;
                }

                for (User manager : departmentManagers) {
                        boolean isActor = actorId != null && actorId.equals(manager.getId());

                        String title;
                        String message;

                        if (action == DepartmentPhaseAction.PUBLISH) {
                                title = isActor ? "Bạn đã công bố giai đoạn sáng kiến của khoa"
                                                : actorDisplayName + " đã công bố giai đoạn sáng kiến của khoa";
                                message = isActor
                                                ? "Bạn vừa công bố các giai đoạn nộp hồ sơ sáng kiến cho đợt \""
                                                                + roundName
                                                                + "\". Hãy tiếp tục theo dõi và hỗ trợ giảng viên."
                                                : actorDisplayName
                                                                + " vừa công bố các giai đoạn nộp hồ sơ sáng kiến cho đợt \""
                                                                + roundName
                                                                + "\". Vui lòng phối hợp hướng dẫn giảng viên thực hiện đúng kế hoạch.";
                        } else {
                                title = isActor ? "Bạn đã đóng giai đoạn sáng kiến của khoa"
                                                : actorDisplayName + " đã đóng giai đoạn sáng kiến của khoa";
                                message = isActor
                                                ? "Bạn vừa đóng các giai đoạn nộp hồ sơ sáng kiến cho đợt \""
                                                                + roundName
                                                                + "\". Vui lòng rà soát hồ sơ và hoàn tất báo cáo khoa."
                                                : actorDisplayName
                                                                + " vừa đóng các giai đoạn nộp hồ sơ sáng kiến cho đợt \""
                                                                + roundName
                                                                + "\". Vui lòng tổng hợp kết quả và chuẩn bị báo cáo khoa.";
                        }

                        Map<String, Object> personalizedData = baseData != null ? new HashMap<>(baseData)
                                        : new HashMap<>();
                        personalizedData.put("actorId", actorId);
                        personalizedData.put("actorName", actorDisplayName);
                        personalizedData.put("isActor", isActor);
                        personalizedData.put("audience", "DEPARTMENT_MANAGERS");

                        sendNotificationToDepartmentUser(
                                        manager,
                                        title,
                                        message,
                                        type,
                                        personalizedData,
                                        department,
                                        MANAGER_TARGET_ROLE);
                }
        }

        private void notifyDepartmentMembersAboutPhaseAction(Department department,
                        List<User> departmentManagers,
                        String actorDisplayName,
                        String roundName,
                        DepartmentPhaseAction action,
                        Map<String, Object> baseData,
                        NotificationTypeEnum type) {

                List<User> users = userRepository.findByDepartmentId(department.getId());
                if (users.isEmpty()) {
                        log.warn("Không có người dùng nào thuộc khoa {}", department.getDepartmentName());
                        return;
                }

                Set<String> managerIds = (departmentManagers == null || departmentManagers.isEmpty())
                                ? Collections.emptySet()
                                : departmentManagers.stream()
                                                .map(User::getId)
                                                .collect(Collectors.toSet());

                List<User> recipients = users.stream()
                                .filter(user -> !managerIds.contains(user.getId()))
                                .collect(Collectors.toList());

                if (recipients.isEmpty()) {
                        log.info("Tất cả người dùng trong khoa {} đều thuộc nhóm quản lý, bỏ qua thông báo chung",
                                        department.getDepartmentName());
                        return;
                }

                String title;
                String message;

                if (action == DepartmentPhaseAction.PUBLISH) {
                        title = "Khoa của bạn đã công bố giai đoạn nộp hồ sơ sáng kiến";
                        message = "Đơn vị của bạn vừa công bố các giai đoạn nộp hồ sơ sáng kiến cho đợt \"" + roundName
                                        + "\". Vui lòng chuẩn bị hồ sơ và nộp đúng kế hoạch.";
                } else {
                        title = "Khoa của bạn đã đóng giai đoạn nộp hồ sơ sáng kiến";
                        message = "Đơn vị của bạn vừa đóng các giai đoạn nộp hồ sơ sáng kiến cho đợt \"" + roundName
                                        + "\". Nếu còn hồ sơ dang dở, vui lòng liên hệ quản lý khoa để được hỗ trợ.";
                }

                Map<String, Object> audienceData = baseData != null ? new HashMap<>(baseData) : new HashMap<>();
                audienceData.put("audience", "DEPARTMENT_MEMBERS");
                audienceData.put("actorName", actorDisplayName);

                Notification notification = createNotification(title, message, type, audienceData, department, null);
                Map<String, Object> wsNotification = createWebSocketNotification(
                                notification.getId(), title, message, type, audienceData);

                for (User user : recipients) {
                        UserNotification userNotification = new UserNotification();
                        userNotification.setUser(user);
                        userNotification.setNotification(notification);
                        userNotification.setIsRead(false);
                        userNotificationRepository.save(userNotification);

                        messagingTemplate.convertAndSend("/queue/notifications/" + user.getId(), wsNotification);
                }
        }

        private void sendNotificationToDepartmentUser(User user,
                        String title,
                        String message,
                        NotificationTypeEnum type,
                        Map<String, Object> data,
                        Department department,
                        String targetRole) {

                Notification notification = createNotification(title, message, type, data, department, targetRole);
                Map<String, Object> wsNotification = createWebSocketNotification(
                                notification.getId(), title, message, type, data);

                UserNotification userNotification = new UserNotification();
                userNotification.setUser(user);
                userNotification.setNotification(notification);
                userNotification.setIsRead(false);
                userNotificationRepository.save(userNotification);

                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, wsNotification);
        }

        public void notifyDepartmentMembersWhenPhaseStarts(DepartmentPhase departmentPhase) {
                Department department = departmentPhase.getDepartment();
                if (department == null) {
                        log.warn("DepartmentPhase '{}' không có khoa, bỏ qua thông báo", departmentPhase.getName());
                        return;
                }

                // Nếu là giai đoạn SCORING, không thông báo cho department members
                // Chỉ thông báo cho thành viên hội đồng thông qua method
                // notifyScoringCommitteeMembersToPrepare
                if (departmentPhase.getPhaseType() == InnovationPhaseTypeEnum.SCORING) {
                        log.info("Giai đoạn SCORING không thông báo cho department members, chỉ thông báo cho hội đồng chấm điểm");
                        return;
                }

                List<User> users = userRepository.findByDepartmentId(department.getId());
                if (users.isEmpty()) {
                        log.warn("Không có người dùng nào thuộc khoa {}", department.getDepartmentName());
                        return;
                }

                List<User> departmentManagers = userRepository.findByDepartmentIdAndRoles(
                                department.getId(),
                                DEPARTMENT_MANAGER_ROLES);

                Set<String> managerIds = (departmentManagers == null || departmentManagers.isEmpty())
                                ? Collections.emptySet()
                                : departmentManagers.stream()
                                                .map(User::getId)
                                                .collect(Collectors.toSet());

                List<User> recipients = users.stream()
                                .filter(user -> !managerIds.contains(user.getId()))
                                .filter(user -> user.getUserRoles().stream()
                                                .noneMatch(userRole -> userRole.getRole()
                                                                .getRoleName() == UserRoleEnum.QUAN_TRI_VIEN_QLKH_HTQT))
                                .collect(Collectors.toList());

                if (recipients.isEmpty()) {
                        log.info("Tất cả người dùng trong khoa {} đều thuộc nhóm quản lý hoặc QUAN_TRI_VIEN_QLKH_HTQT, bỏ qua thông báo",
                                        department.getDepartmentName());
                        return;
                }

                String roundName = departmentPhase.getInnovationRound() != null
                                ? departmentPhase.getInnovationRound().getName()
                                : "đợt sáng kiến";

                String title = "Giai đoạn sáng kiến đã bắt đầu";
                String message = "Giai đoạn \"" + departmentPhase.getName() + "\" của đợt \"" + roundName
                                + "\" đã bắt đầu từ hôm nay. Vui lòng chuẩn bị và nộp hồ sơ đúng thời hạn.";

                Map<String, Object> data = new HashMap<>();
                data.put("departmentId", department.getId());
                data.put("departmentName", department.getDepartmentName());
                data.put("phaseId", departmentPhase.getId());
                data.put("phaseName", departmentPhase.getName());
                data.put("phaseType",
                                departmentPhase.getPhaseType() != null ? departmentPhase.getPhaseType().name() : null);
                data.put("roundName", roundName);
                data.put("roundId", departmentPhase.getInnovationRound() != null
                                ? departmentPhase.getInnovationRound().getId()
                                : null);
                data.put("phaseStartDate", departmentPhase.getPhaseStartDate() != null
                                ? departmentPhase.getPhaseStartDate().toString()
                                : null);
                data.put("phaseEndDate", departmentPhase.getPhaseEndDate() != null
                                ? departmentPhase.getPhaseEndDate().toString()
                                : null);
                data.put("action", "phase_started");
                data.put("url", "/department-phases?departmentId=" + department.getId());
                data.put("audience", "DEPARTMENT_MEMBERS");

                Notification notification = createNotification(title, message,
                                NotificationTypeEnum.DEPARTMENT_PHASE_PUBLISHED,
                                data, department, null);
                Map<String, Object> wsNotification = createWebSocketNotification(
                                notification.getId(), title, message, NotificationTypeEnum.DEPARTMENT_PHASE_PUBLISHED,
                                data);

                for (User user : recipients) {
                        UserNotification userNotification = new UserNotification();
                        userNotification.setUser(user);
                        userNotification.setNotification(notification);
                        userNotification.setIsRead(false);
                        userNotificationRepository.save(userNotification);

                        messagingTemplate.convertAndSend("/queue/notifications/" + user.getId(), wsNotification);
                }

                log.info("Đã gửi thông báo bắt đầu phase '{}' đến {} giảng viên thuộc khoa {}",
                                departmentPhase.getName(), recipients.size(), department.getDepartmentName());
        }

        public void notifyDepartmentManagersToEstablishScoringCommittee(DepartmentPhase submissionPhase) {
                Department department = submissionPhase.getDepartment();
                if (department == null) {
                        log.warn("DepartmentPhase '{}' không có khoa, bỏ qua thông báo", submissionPhase.getName());
                        return;
                }

                if (submissionPhase.getInnovationRound() == null) {
                        log.warn("DepartmentPhase '{}' không có innovation round, bỏ qua thông báo",
                                        submissionPhase.getName());
                        return;
                }

                String roundId = submissionPhase.getInnovationRound().getId();
                String roundName = submissionPhase.getInnovationRound().getName() != null
                                ? submissionPhase.getInnovationRound().getName()
                                : "đợt sáng kiến";

                Optional<DepartmentPhase> scoringPhaseOpt = departmentPhaseRepository
                                .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                                                department.getId(),
                                                roundId,
                                                InnovationPhaseTypeEnum.SCORING);

                if (scoringPhaseOpt.isEmpty()) {
                        log.warn("Không tìm thấy phase SCORING cho khoa {} và round {}, bỏ qua thông báo",
                                        department.getDepartmentName(), roundId);
                        return;
                }

                DepartmentPhase scoringPhase = scoringPhaseOpt.get();
                LocalDate scoringStartDate = scoringPhase.getPhaseStartDate();
                LocalDate submissionEndDate = submissionPhase.getPhaseEndDate();

                String title = "Cần thành lập hội đồng chấm điểm";
                String message = String.format(
                                "Giai đoạn nộp hồ sơ \"%s\" của đợt \"%s\" sẽ kết thúc vào ngày %s. "
                                                + "Giai đoạn chấm điểm sẽ bắt đầu vào ngày %s. "
                                                + "Vui lòng thành lập hội đồng chấm điểm trước khi giai đoạn chấm điểm bắt đầu.",
                                submissionPhase.getName(),
                                roundName,
                                submissionEndDate != null ? submissionEndDate.toString() : "N/A",
                                scoringStartDate != null ? scoringStartDate.toString() : "N/A");

                Map<String, Object> data = new HashMap<>();
                data.put("departmentId", department.getId());
                data.put("departmentName", department.getDepartmentName());
                data.put("submissionPhaseId", submissionPhase.getId());
                data.put("submissionPhaseName", submissionPhase.getName());
                data.put("submissionPhaseEndDate", submissionEndDate != null ? submissionEndDate.toString() : null);
                data.put("scoringPhaseId", scoringPhase.getId());
                data.put("scoringPhaseName", scoringPhase.getName());
                data.put("scoringPhaseStartDate", scoringStartDate != null ? scoringStartDate.toString() : null);
                data.put("roundId", roundId);
                data.put("roundName", roundName);
                data.put("action", "establish_scoring_committee");
                data.put("url", "/department-phases?departmentId=" + department.getId());
                data.put("audience", "DEPARTMENT_MANAGERS");

                notifyUsersByDepartmentAndRoles(
                                department.getId(),
                                List.of(UserRoleEnum.TRUONG_KHOA),
                                title,
                                message,
                                NotificationTypeEnum.SYSTEM_ANNOUNCEMENT,
                                data);

                log.info("Đã gửi thông báo thành lập hội đồng chấm điểm cho TRUONG_KHOA của khoa {}",
                                department.getDepartmentName());
        }

        public void notifyScoringCommitteeMembersToPrepare(DepartmentPhase scoringPhase) {
                Department department = scoringPhase.getDepartment();
                if (department == null) {
                        log.warn("DepartmentPhase '{}' không có khoa, bỏ qua thông báo", scoringPhase.getName());
                        return;
                }

                LocalDate scoringStartDate = scoringPhase.getPhaseStartDate();
                if (scoringStartDate == null) {
                        log.warn("DepartmentPhase '{}' không có ngày bắt đầu, bỏ qua thông báo",
                                        scoringPhase.getName());
                        return;
                }

                String roundName = scoringPhase.getInnovationRound() != null
                                ? scoringPhase.getInnovationRound().getName()
                                : "đợt sáng kiến";

                String title = "Chuẩn bị thực hiện chấm điểm sáng kiến";
                String message = String.format(
                                "Giai đoạn chấm điểm \"%s\" của đợt \"%s\" sẽ diễn ra vào ngày %s. "
                                                + "Bạn cần phải chuẩn bị thực hiện chấm điểm.",
                                scoringPhase.getName(),
                                roundName,
                                scoringStartDate.toString());

                Map<String, Object> data = new HashMap<>();
                data.put("departmentId", department.getId());
                data.put("departmentName", department.getDepartmentName());
                data.put("phaseId", scoringPhase.getId());
                data.put("phaseName", scoringPhase.getName());
                data.put("phaseType", InnovationPhaseTypeEnum.SCORING.name());
                data.put("phaseStartDate", scoringStartDate.toString());
                data.put("phaseEndDate",
                                scoringPhase.getPhaseEndDate() != null ? scoringPhase.getPhaseEndDate().toString()
                                                : null);
                data.put("roundId", scoringPhase.getInnovationRound() != null
                                ? scoringPhase.getInnovationRound().getId()
                                : null);
                data.put("roundName", roundName);
                data.put("action", "prepare_scoring");
                data.put("url", "/department-phases?departmentId=" + department.getId());
                data.put("audience", "SCORING_COMMITTEE_MEMBERS");

                notifyUsersByDepartmentAndRoles(
                                department.getId(),
                                List.of(UserRoleEnum.TV_HOI_DONG_KHOA),
                                title,
                                message,
                                NotificationTypeEnum.SYSTEM_ANNOUNCEMENT,
                                data);

                log.info("Đã gửi thông báo chuẩn bị chấm điểm cho TV_HOI_DONG_KHOA của khoa {}",
                                department.getDepartmentName());
        }

        /**
         * Thông báo cho TRUONG_KHOA khi giai đoạn SUBMISSION sắp kết thúc hoặc kết thúc
         * 
         * @param submissionPhase Phase SUBMISSION
         * @param isOneDayBefore  true = thông báo trước 1 ngày, false = thông báo vào
         *                        ngày kết thúc
         */
        public void notifySubmissionPhaseEnding(DepartmentPhase submissionPhase, boolean isOneDayBefore) {
                Department department = submissionPhase.getDepartment();
                if (department == null) {
                        log.warn("DepartmentPhase '{}' không có khoa, bỏ qua thông báo", submissionPhase.getName());
                        return;
                }

                if (submissionPhase.getInnovationRound() == null) {
                        log.warn("DepartmentPhase '{}' không có innovation round, bỏ qua thông báo",
                                        submissionPhase.getName());
                        return;
                }

                String roundName = submissionPhase.getInnovationRound().getName() != null
                                ? submissionPhase.getInnovationRound().getName()
                                : "đợt sáng kiến";
                LocalDate submissionEndDate = submissionPhase.getPhaseEndDate();

                String title;
                String message;

                if (isOneDayBefore) {
                        title = "Giai đoạn nộp hồ sơ sắp kết thúc";
                        message = String.format(
                                        "Giai đoạn nộp hồ sơ \"%s\" của đợt \"%s\" sẽ kết thúc vào ngày mai (%s). "
                                                        + "Vui lòng kiểm tra và nhắc nhở giảng viên hoàn tất hồ sơ.",
                                        submissionPhase.getName(),
                                        roundName,
                                        submissionEndDate != null ? submissionEndDate.toString() : "N/A");
                } else {
                        title = "Giai đoạn nộp hồ sơ đã kết thúc";
                        message = String.format(
                                        "Giai đoạn nộp hồ sơ \"%s\" của đợt \"%s\" đã kết thúc hôm nay (%s). "
                                                        + "Vui lòng rà soát danh sách hồ sơ đã nộp và chuẩn bị cho giai đoạn chấm điểm.",
                                        submissionPhase.getName(),
                                        roundName,
                                        submissionEndDate != null ? submissionEndDate.toString() : "N/A");
                }

                Map<String, Object> data = new HashMap<>();
                data.put("departmentId", department.getId());
                data.put("departmentName", department.getDepartmentName());
                data.put("submissionPhaseId", submissionPhase.getId());
                data.put("submissionPhaseName", submissionPhase.getName());
                data.put("submissionPhaseEndDate", submissionEndDate != null ? submissionEndDate.toString() : null);
                data.put("roundId", submissionPhase.getInnovationRound().getId());
                data.put("roundName", roundName);
                data.put("action", isOneDayBefore ? "submission_ending_soon" : "submission_ended");
                data.put("url", "/department-phases?departmentId=" + department.getId());
                data.put("audience", "DEPARTMENT_MANAGERS");
                data.put("isOneDayBefore", isOneDayBefore);

                notifyUsersByDepartmentAndRoles(
                                department.getId(),
                                List.of(UserRoleEnum.TRUONG_KHOA),
                                title,
                                message,
                                NotificationTypeEnum.SYSTEM_ANNOUNCEMENT,
                                data);

                log.info("Đã gửi thông báo {} giai đoạn nộp hồ sơ cho TRUONG_KHOA của khoa {}",
                                isOneDayBefore ? "sắp kết thúc" : "đã kết thúc",
                                department.getDepartmentName());
        }

        @Transactional
        public void notifyUserOnInnovationCreated(String userId, String innovationId, String innovationName,
                        InnovationStatusEnum status) {
                try {
                        User user = userRepository.findById(userId)
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy user với ID: " + userId));

                        String statusDisplayName = getStatusDisplayName(status);

                        String title = "Bạn đã tạo sáng kiến thành công";
                        String message = String.format(
                                        "Bạn vừa tạo sáng kiến \"%s\" thành công. Sáng kiến đang ở trạng thái: %s",
                                        innovationName, statusDisplayName);

                        Map<String, Object> data = new HashMap<>();
                        data.put("innovationId", innovationId);
                        data.put("innovationName", innovationName);
                        data.put("status", status.name());
                        data.put("statusDisplayName", statusDisplayName);
                        data.put("action", "innovation_created");
                        data.put("url", "/innovations/" + innovationId);

                        Notification notification = createNotification(title, message,
                                        NotificationTypeEnum.INNOVATION_SUBMITTED, data, user.getDepartment(), null);

                        Map<String, Object> wsNotification = createWebSocketNotification(
                                        notification.getId(), title, message, NotificationTypeEnum.INNOVATION_SUBMITTED,
                                        data);

                        UserNotification userNotification = new UserNotification();
                        userNotification.setUser(user);
                        userNotification.setNotification(notification);
                        userNotification.setIsRead(false);
                        userNotificationRepository.save(userNotification);

                        String userDestination = "/queue/notifications/" + user.getId();
                        messagingTemplate.convertAndSend(userDestination, wsNotification);

                        log.info("Đã gửi thông báo tạo sáng kiến thành công cho user: {} (Innovation: {})",
                                        user.getFullName(), innovationName);
                } catch (Exception e) {
                        log.error("Lỗi khi gửi thông báo tạo sáng kiến cho user {}: {}", userId, e.getMessage(), e);
                }
        }

        private String getStatusDisplayName(InnovationStatusEnum status) {
                if (status == null) {
                        return "Không xác định";
                }
                return switch (status) {
                        case DRAFT -> "Nháp";
                        case SUBMITTED -> "Đã nộp";
                        case PENDING_KHOA_REVIEW -> "Chờ khoa duyệt";
                        case KHOA_APPROVED -> "Khoa đã phê duyệt";
                        case KHOA_REJECTED -> "Khoa đã từ chối";
                        case PENDING_TRUONG_REVIEW -> "Chờ trường duyệt";
                        case TRUONG_APPROVED -> "Trường đã phê duyệt";
                        case TRUONG_REJECTED -> "Trường đã từ chối";
                        case FINAL_APPROVED -> "Đã phê duyệt cuối cùng";
                };
        }

        private enum DepartmentPhaseAction {
                PUBLISH,
                CLOSE
        }

        /**
         * Gửi thông báo cho tác giả về kết quả đánh giá của hội đồng
         */
        @Transactional
        public void notifyAuthorAboutCouncilResult(String innovationId, String innovationName,
                        String authorId, ReviewLevelEnum councilLevel, Boolean isApproved, String councilName) {
                try {
                        User author = userRepository.findById(authorId)
                                        .orElseThrow(() -> new IdInvalidException(
                                                        "Không tìm thấy tác giả với ID: " + authorId));

                        String councilLevelName = councilLevel == ReviewLevelEnum.KHOA ? "Khoa" : "Trường";
                        String statusText = isApproved ? "thông qua" : "không thông qua";

                        String title = String.format("Kết quả đánh giá sáng kiến từ hội đồng %s", councilLevelName);
                        String message = String.format(
                                        "Sáng kiến \"%s\" của bạn đã được hội đồng %s \"%s\" đánh giá và %s.",
                                        innovationName, councilLevelName, councilName, statusText);

                        Map<String, Object> data = new HashMap<>();
                        data.put("innovationId", innovationId);
                        data.put("innovationName", innovationName);
                        data.put("councilLevel", councilLevel.name());
                        data.put("councilName", councilName);
                        data.put("isApproved", isApproved);
                        data.put("statusText", statusText);

                        NotificationTypeEnum notificationType = isApproved
                                        ? NotificationTypeEnum.INNOVATION_APPROVED
                                        : NotificationTypeEnum.INNOVATION_REJECTED;

                        Notification notification = createNotification(title, message, notificationType,
                                        data, null, null);

                        Map<String, Object> wsNotification = createWebSocketNotification(
                                        notification.getId(), title, message, notificationType, data);

                        UserNotification userNotification = new UserNotification();
                        userNotification.setUser(author);
                        userNotification.setNotification(notification);
                        userNotification.setIsRead(false);
                        userNotificationRepository.save(userNotification);

                        String userDestination = "/queue/notifications/" + author.getId();
                        messagingTemplate.convertAndSend(userDestination, wsNotification);

                        log.info("Đã gửi thông báo kết quả đánh giá cho tác giả {} về sáng kiến {}", authorId,
                                        innovationId);
                } catch (Exception e) {
                        log.error("Lỗi khi gửi thông báo kết quả đánh giá cho tác giả {} về sáng kiến {}: {}",
                                        authorId, innovationId, e.getMessage(), e);
                }
        }
}
