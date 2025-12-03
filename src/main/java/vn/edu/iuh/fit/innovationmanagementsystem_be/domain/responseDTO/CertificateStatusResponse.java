package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.RevocationReasonEnum;

import java.time.LocalDateTime;

public class CertificateStatusResponse {
    private String certificateSerial;
    private boolean revoked;
    private LocalDateTime revocationDate;
    private RevocationReasonEnum revocationReason;
    private String revokedBy;
    private String notes;

    public String getCertificateSerial() {
        return certificateSerial;
    }

    public void setCertificateSerial(String certificateSerial) {
        this.certificateSerial = certificateSerial;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getRevocationDate() {
        return revocationDate;
    }

    public void setRevocationDate(LocalDateTime revocationDate) {
        this.revocationDate = revocationDate;
    }

    public RevocationReasonEnum getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(RevocationReasonEnum revocationReason) {
        this.revocationReason = revocationReason;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
