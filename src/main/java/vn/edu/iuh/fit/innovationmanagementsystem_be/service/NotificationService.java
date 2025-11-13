package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void notifyUsersByRole(UserRoleEnum role, String message, Map<String, Object> data) {
        try {
            List<User> users = userRepository.findUsersByRole(role);

            if (users.isEmpty()) {
                throw new IdInvalidException("Không tìm thấy user nào có role: " + role);
            }

            Map<String, Object> notification = new HashMap<>();
            notification.put("message", message);
            notification.put("data", data);
            notification.put("timestamp", System.currentTimeMillis());

            String destination = "/topic/notifications/" + role.name().toLowerCase();

            for (User user : users) {
                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, notification);
                log.info("Đã gửi notification đến user: {} (ID: {})", user.getFullName(), user.getId());
            }

            messagingTemplate.convertAndSend(destination, notification);
            log.info("Đã gửi notification đến {} users có role: {}", users.size(), role);
        } catch (Exception e) {
            log.error("Lỗi khi gửi notification đến users có role {}: {}", role, e.getMessage(), e);
            throw new IdInvalidException("Lỗi khi gửi notification đến users có role: " + role, e);
        }
    }

    public void notifyRoundPublished(String roundId, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roundId", roundId);
        data.put("roundName", roundName);
        data.put("type", "ROUND_PUBLISHED");
        data.put("action", "publish");

        String message = "Đợt sáng kiến '" + roundName + "' đã được công bố";
        notifyUsersByRole(UserRoleEnum.TRUONG_KHOA, message, data);
    }

    public void notifyRoundClosed(String roundId, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roundId", roundId);
        data.put("roundName", roundName);
        data.put("type", "ROUND_CLOSED");
        data.put("action", "close");

        String message = "Đợt sáng kiến '" + roundName + "' đã được đóng";
        notifyUsersByRole(UserRoleEnum.TRUONG_KHOA, message, data);
    }

    public void notifyUsersByDepartment(String departmentId, String message, Map<String, Object> data) {
        try {
            List<User> users = userRepository.findByDepartmentId(departmentId);

            if (users.isEmpty()) {
                throw new IdInvalidException("Không tìm thấy user nào thuộc khoa với ID: " + departmentId);
            }

            Map<String, Object> notification = new HashMap<>();
            notification.put("message", message);
            notification.put("data", data);
            notification.put("timestamp", System.currentTimeMillis());

            String destination = "/topic/notifications/department/" + departmentId;

            for (User user : users) {
                String userDestination = "/queue/notifications/" + user.getId();
                messagingTemplate.convertAndSend(userDestination, notification);
                log.info("Đã gửi notification đến user: {} (ID: {})", user.getFullName(), user.getId());
            }

            messagingTemplate.convertAndSend(destination, notification);
            log.info("Đã gửi notification đến {} users thuộc khoa ID: {}", users.size(), departmentId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi notification đến users thuộc khoa {}: {}", departmentId, e.getMessage(), e);
            throw new IdInvalidException("Lỗi khi gửi notification đến users thuộc khoa: " + departmentId, e);
        }
    }

    public void notifyDepartmentPhasePublished(String departmentId, String departmentName, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("departmentId", departmentId);
        data.put("departmentName", departmentName);
        data.put("roundName", roundName);
        data.put("type", "DEPARTMENT_PHASE_PUBLISHED");
        data.put("action", "publish");

        String message = "Khoa " + departmentName + " đã công bố giai đoạn cho đợt sáng kiến '" + roundName + "'";
        notifyUsersByDepartment(departmentId, message, data);
    }

    public void notifyDepartmentPhaseClosed(String departmentId, String departmentName, String roundName) {
        Map<String, Object> data = new HashMap<>();
        data.put("departmentId", departmentId);
        data.put("departmentName", departmentName);
        data.put("roundName", roundName);
        data.put("type", "DEPARTMENT_PHASE_CLOSED");
        data.put("action", "close");

        String message = "Khoa " + departmentName + " đã đóng giai đoạn cho đợt sáng kiến '" + roundName + "'";
        notifyUsersByDepartment(departmentId, message, data);
    }
}
