package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum PhaseStatusEnum {

    DRAFT("Bản nháp", "Giai đoạn đang ở trạng thái bản nháp"),

    ACTIVE("Đang hoạt động", "Giai đoạn đang diễn ra"),

    COMPLETED("Đã hoàn thành", "Giai đoạn đã kết thúc thành công");

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
