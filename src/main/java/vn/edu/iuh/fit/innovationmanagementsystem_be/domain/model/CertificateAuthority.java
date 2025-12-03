package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "certificate_authorities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CertificateAuthority extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "certificate_data", nullable = false, columnDefinition = "TEXT")
    private String certificateData;

    @Column(name = "certificate_serial", nullable = false, unique = true, columnDefinition = "VARCHAR(255)")
    private String certificateSerial;

    @Column(name = "certificate_issuer", columnDefinition = "VARCHAR(500)")
    private String certificateIssuer;

    @Column(name = "certificate_subject", columnDefinition = "VARCHAR(500)")
    private String certificateSubject;

    @Column(name = "valid_from", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CAStatusEnum status = CAStatusEnum.PENDING;

    @Column(name = "verified_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by", columnDefinition = "VARCHAR(255)")
    private String verifiedBy;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "certificateAuthority", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSignatureProfile> userSignatureProfiles = new ArrayList<>();
}
