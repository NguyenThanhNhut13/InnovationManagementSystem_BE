package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum ScoringPeriodStatusEnum {
    NOT_STARTED("Chưa đến thời gian chấm điểm"),
    ACTIVE("Đang trong thời gian chấm điểm"),
    ENDED("Đã hết thời gian chấm điểm"),
    PREVIEW("Xem trước (3 ngày trước khi bắt đầu)");

    private final String displayName;

    ScoringPeriodStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

