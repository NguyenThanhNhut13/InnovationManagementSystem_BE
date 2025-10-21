package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums;

public enum TargetRoleCode {
    EMPLOYEE("EMPLOYEE"),
    DEPARTMENT("DEPARTMENT"),
    QLKH_SECRETARY("QLKH_SECRETARY");

    private final String value;

    TargetRoleCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
