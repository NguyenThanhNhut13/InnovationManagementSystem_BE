package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CAStatusEnum;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity
@Table(name = "user_signature_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSignatureProfile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "path_url")
    private String pathUrl;

    @Column(name = "encrypted_private_key", columnDefinition = "TEXT")
    private String encryptedPrivateKey; // Private key

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "certificate_version")
    private Integer certificateVersion; // Version của certificate (1, 2, 3)

    @Column(name = "certificate_serial", nullable = false)
    private String certificateSerial; // Số serial của chứng chỉ

    @Column(name = "certificate_issuer")
    private String certificateIssuer; // Tổ chức phát hành chứng chỉ

    @Column(name = "certificate_subject")
    private String certificateSubject; // Subject name của certificate

    @Column(name = "certificate_valid_from")
    private LocalDateTime certificateValidFrom; // Ngày bắt đầu hiệu lực certificate

    @Column(name = "certificate_expiry_date")
    private LocalDateTime certificateExpiryDate; // Ngày hết hạn certificate

    @Column(name = "certificate_data", columnDefinition = "TEXT")
    private String certificateData; // X.509 Certificate data (Base64)

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_status", nullable = false)
    private CAStatusEnum certificateStatus; // Status: VALID, EXPIRED, REVOKED, etc.

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_authority_id")
    private CertificateAuthority certificateAuthority;

    @OneToMany(mappedBy = "userSignatureProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();
}