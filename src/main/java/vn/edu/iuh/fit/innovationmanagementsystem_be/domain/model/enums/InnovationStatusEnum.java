package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum InnovationStatusEnum {
    DRAFT, // Bản nháp
    SUBMITTED, // Đã nộp
    PENDING_KHOA_REVIEW, // Chờ Khoa duyệt
    RETURNED_TO_SUBMITTER, // Trả về người nộp
    KHOA_REVIEWED, // Khoa đã duyệt
    KHOA_APPROVED, // Khoa phê duyệt
    KHOA_REJECTED, // Khoa từ chối
    PENDING_TRUONG_REVIEW, // Chờ Trường duyệt
    TRUONG_REVIEWED, // Trường đã duyệt
    TRUONG_APPROVED, // Trường phê duyệt
    TRUONG_REJECTED, // Trường từ chối
    FINAL_APPROVED // Phê duyệt cuối cùng
}
