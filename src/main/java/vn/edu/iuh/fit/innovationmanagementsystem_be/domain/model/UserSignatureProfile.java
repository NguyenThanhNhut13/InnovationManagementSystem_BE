package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

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

    @Column(name = "private_key", columnDefinition = "TEXT")
    private String privateKey;

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "certificate_serial", nullable = false)
    private String certificateSerial; // Số serial của chứng chỉ

    @Column(name = "certificate_issuer")
    private String certificateIssuer; // Tổ chức phát hành chứng chỉ

    @Column(name = "certificate_valid_from")
    private LocalDateTime certificateValidFrom; // Thời gian có hiệu lực

    @Column(name = "certificate_valid_to")
    private LocalDateTime certificateValidTo; // Thời gian hết hạn

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "userSignatureProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();
}