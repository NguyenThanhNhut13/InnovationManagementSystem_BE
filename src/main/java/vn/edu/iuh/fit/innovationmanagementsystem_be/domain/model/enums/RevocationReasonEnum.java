package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum RevocationReasonEnum {
    USER_INACTIVE("User không còn hoạt động"),
    USER_SUSPENDED("User bị tạm khóa"),
    KEY_COMPROMISE("Private key bị lộ"),
    CERTIFICATE_EXPIRED("Certificate hết hạn"),
    SUPERSEDED("Certificate bị thay thế"),
    CESSATION_OF_OPERATION("Ngừng hoạt động"),
    ADMIN_REVOKED("Thu hồi bởi admin");

    private final String description;

    RevocationReasonEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return description;
    }
}
