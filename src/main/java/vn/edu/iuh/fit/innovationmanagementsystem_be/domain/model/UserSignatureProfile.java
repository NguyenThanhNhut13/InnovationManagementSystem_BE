package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.List;
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
    private String encryptedPrivateKey; // Private key đã được mã hóa bằng HSM

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "certificate_serial", nullable = false)
    private String certificateSerial; // Số serial của chứng chỉ

    @Column(name = "certificate_issuer")
    private String certificateIssuer; // Tổ chức phát hành chứng chỉ

    @Column(name = "certificate_data", columnDefinition = "TEXT")
    private String certificateData; // X.509 Certificate data (Base64)

    @Column(name = "certificate_chain", columnDefinition = "TEXT")
    private String certificateChain; // Certificate chain (JSON format)

    @Column(name = "certificate_expiry_date")
    private java.time.LocalDateTime certificateExpiryDate; // Ngày hết hạn certificate

    @Column(name = "certificate_status")
    private String certificateStatus; // Status: VALID, EXPIRED, REVOKED, etc.

    @Column(name = "last_certificate_validation")
    private java.time.LocalDateTime lastCertificateValidation; // Lần cuối validate certificate

    // TSA fields removed for academic project

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "userSignatureProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();
}