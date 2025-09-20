package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum PhaseStatusEnum {

    PENDING("Chờ bắt đầu", "Giai đoạn chờ đến thời gian bắt đầu"),

    ACTIVE("Đang hoạt động", "Giai đoạn đang diễn ra"),

    COMPLETED("Đã hoàn thành", "Giai đoạn đã kết thúc thành công"),

    CANCELLED("Đã hủy", "Giai đoạn đã bị hủy"),

    SUSPENDED("Tạm dừng", "Giai đoạn tạm thời bị dừng");

    private final String displayName;
    private final String description;

    PhaseStatusEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
