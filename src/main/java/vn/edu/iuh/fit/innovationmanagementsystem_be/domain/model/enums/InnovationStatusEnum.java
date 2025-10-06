package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum InnovationStatusEnum {
    DRAFT,
    SUBMITTED,

    // Khoa Duyệt
    // Chờ thư ký khoa ký
    PENDING_KHOA_REVIEW, // Chờ Khoa duyệt
    KHOA_RETURNED_TO_SUBMITTER, // Khoa trả về người nộp để chỉnh sửa
    KHOA_APPROVED, // Khoa phê duyệt
    KHOA_REJECTED, // Khoa từ chối

    PENDING_TRUONG_REVIEW, // Chờ Trường duyệt
    TRUONG_RETURNED_TO_SUBMITTER, // Trường trả về người nộp để chỉnh sửa
    TRUONG_APPROVED, // Trường phê duyệt
    TRUONG_REJECTED, // Trường từ chối

    FINAL_APPROVED // Phê duyệt cuối cùng
}
