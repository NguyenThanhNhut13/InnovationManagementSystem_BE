package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.NotificationTypeEnum;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private NotificationTypeEnum type;
    private String referenceId;
    private String referenceType;
    private String departmentId;
    private String departmentName;
    private String targetRole;
    private LocalDateTime createdAt;
    private Map<String, Object> data;
    private Boolean isRead;
    private LocalDateTime readAt;
}
