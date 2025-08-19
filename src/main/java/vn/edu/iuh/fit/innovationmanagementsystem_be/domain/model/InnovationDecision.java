package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "innovation_decision")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "decision_number", nullable = false)
    private String decisionNumber;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "promulgated_date", nullable = false)
    private LocalDate promulgatedDate;

    @Column(name = "signed_by", nullable = false)
    private String signedBy;

    @Column(name = "bases", columnDefinition = "TEXT")
    private String bases;

    @Column(name = "year_decision")
    private Integer yearDecision;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Relationships
    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InnovationRound> innovationRounds = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Regulation> regulations = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReviewScore> reviewScores = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Chapter> chapters = new ArrayList<>();

    // Pre-persist and pre-update methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();

        String currentUserId = getCurrentUserId();
        createdBy = currentUserId;
        updatedBy = currentUserId;
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();

        updatedBy = getCurrentUserId();
    }

    /**
     * Lấy ID của user hiện tại từ SecurityContext.
     * Nếu không có user đăng nhập, trả về "SYSTEM".
     */
    private String getCurrentUserId() {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                } else if (principal instanceof String) {
                    return (String) principal;
                }
            }
        } catch (Exception ignored) {
        }
        return "SYSTEM";
    }
}