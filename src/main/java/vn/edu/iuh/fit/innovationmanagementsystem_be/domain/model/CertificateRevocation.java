package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.RevocationReasonEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_revocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CertificateRevocation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "certificate_serial", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    private String certificateSerial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "revocation_date", nullable = false)
    private LocalDateTime revocationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "revocation_reason", nullable = false, columnDefinition = "VARCHAR(50)")
    private RevocationReasonEnum revocationReason;

    @Column(name = "revoked_by", columnDefinition = "VARCHAR(36)")
    private String revokedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
