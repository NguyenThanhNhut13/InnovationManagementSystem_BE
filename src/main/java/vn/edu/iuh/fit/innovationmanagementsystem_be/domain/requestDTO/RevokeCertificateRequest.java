package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.RevocationReasonEnum;

public class RevokeCertificateRequest {
    private String userId;
    private RevocationReasonEnum reason;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public RevocationReasonEnum getReason() {
        return reason;
    }

    public void setReason(RevocationReasonEnum reason) {
        this.reason = reason;
    }
}
