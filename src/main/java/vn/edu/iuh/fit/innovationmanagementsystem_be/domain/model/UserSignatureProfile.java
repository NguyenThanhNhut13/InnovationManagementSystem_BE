package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "user_signature_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignatureProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "path_url")
    private String pathUrl;

    @Column(name = "private_key")
    private String privateKey;

    @Column(name = "public_key")
    private String publicKey;

    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Relationships
    @OneToMany(mappedBy = "userSignatureProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DigitalSignature> digitalSignatures;

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pre-persist and pre-update methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}