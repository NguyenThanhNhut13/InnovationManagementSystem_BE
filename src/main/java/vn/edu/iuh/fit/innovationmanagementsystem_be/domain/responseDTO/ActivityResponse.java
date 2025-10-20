package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private String id;
    private String innovationId;
    private String innovationName;
    private InnovationStatusEnum activityType;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String timeAgo;
    private String iconType;
    private String iconColor;
}
