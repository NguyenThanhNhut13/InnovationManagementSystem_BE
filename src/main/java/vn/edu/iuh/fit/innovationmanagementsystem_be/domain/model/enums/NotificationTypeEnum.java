package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum NotificationTypeEnum {
    ROUND_PUBLISHED("Công bố đợt sáng kiến"),
    ROUND_CLOSED("Đóng đợt sáng kiến"),
    DEPARTMENT_PHASE_PUBLISHED("Công bố giai đoạn khoa"),
    DEPARTMENT_PHASE_CLOSED("Đóng giai đoạn khoa"),
    INNOVATION_SUBMITTED("Nộp sáng kiến"),
    INNOVATION_APPROVED("Phê duyệt sáng kiến"),
    INNOVATION_REJECTED("Từ chối sáng kiến"),
    SYSTEM_ANNOUNCEMENT("Thông báo hệ thống"),
    OTHER("Khác");

    private final String value;

    NotificationTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
