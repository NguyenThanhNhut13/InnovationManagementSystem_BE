package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum InnovationPhaseEnum {

    SUBMISSION("Nộp hồ sơ", "Giai đoạn các tác giả nộp hồ sơ đề nghị xét công nhận sáng kiến về đơn vị"),

    DEPARTMENT_EVALUATION("Đánh giá cấp khoa",
            "Giai đoạn đơn vị tổ chức họp toàn thể đánh giá khả năng, phạm vi và hiệu quả áp dụng của sáng kiến"),

    DOCUMENT_SUBMISSION("Gửi hồ sơ lên trường",
            "Giai đoạn các đơn vị gửi hồ sơ đề nghị xét, công nhận sáng kiến về thường trực Hội đồng Sáng kiến"),

    COUNCIL_REVIEW("Hội đồng xem xét",
            "Giai đoạn thường trực Hội đồng Sáng kiến gửi hồ sơ cho các thành viên nghiên cứu trước khi họp"),

    COUNCIL_MEETING("Họp hội đồng", "Giai đoạn Hội đồng Sáng kiến họp xem xét, đánh giá nội dung các sáng kiến"),

    PRINCIPAL_APPROVAL("Hiệu trưởng phê duyệt", "Giai đoạn Hiệu trưởng xem xét, quyết định công nhận sáng kiến"),

    RESULT_ANNOUNCEMENT("Công bố kết quả",
            "Giai đoạn công khai các sáng kiến được công nhận trên trang E-office của Nhà trường");

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
