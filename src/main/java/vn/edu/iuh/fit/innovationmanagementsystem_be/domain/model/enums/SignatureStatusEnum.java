package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum SignatureStatusEnum {
    PENDING, // Chờ ký
    SIGNED, // Đã ký
    VERIFIED, // Đã xác thực
    EXPIRED, // Hết hạn
    REVOKED // Bị thu hồi
}
