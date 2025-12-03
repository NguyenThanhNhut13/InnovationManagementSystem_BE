package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;

public class UpdateUserStatusRequest {
    private UserStatusEnum status;

    public UserStatusEnum getStatus() {
        return status;
    }

    public void setStatus(UserStatusEnum status) {
        this.status = status;
    }
}
