package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.SignatureStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "digital_signatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DigitalSignature extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentTypeEnum documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "signed_as_role")
    private UserRoleEnum signedAsRole;

    @Column(name = "sign_at")
    private LocalDateTime signAt;

    @Column(name = "signature_hash", nullable = false, unique = true)
    private String signatureHash; // Hash duy nhất của chữ ký

    @Column(name = "document_hash", nullable = false)
    private String documentHash; // Hash của tài liệu trước khi ký

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SignatureStatusEnum status = SignatureStatusEnum.PENDING; // Trạng thái chữ ký

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_id")
    private Innovation innovation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_signature_profile_id", nullable = false)
    private UserSignatureProfile userSignatureProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @PrePersist
    protected void ensureSignAt() {
        if (signAt == null) {
            signAt = LocalDateTime.now();
        }
    }
}