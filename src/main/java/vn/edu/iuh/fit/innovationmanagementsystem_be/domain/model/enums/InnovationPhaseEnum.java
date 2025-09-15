package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum InnovationPhaseEnum {

    SUBMISSION("Nộp hồ sơ", "Giai đoạn các tác giả nộp hồ sơ sáng kiến"),

    DEPARTMENT_REVIEW("Chấm cấp khoa", "Giai đoạn hội đồng khoa chấm điểm và đánh giá"),

    UNIVERSITY_REVIEW("Chấm cấp trường", "Giai đoạn hội đồng trường chấm điểm và đánh giá"),

    RESULT_ANNOUNCEMENT("Công bố kết quả", "Giai đoạn công bố kết quả cuối cùng");

    private final String displayName;
    private final String description;

    InnovationPhaseEnum(String displayName, String description) {
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
