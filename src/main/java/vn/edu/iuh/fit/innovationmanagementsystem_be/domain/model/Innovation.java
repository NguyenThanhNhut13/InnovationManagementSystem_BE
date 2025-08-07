package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "innovations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Innovation {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "is_score")
    private Boolean isScore;

    @Column(name = "innovation_name", nullable = false)
    private String innovationName;

    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innovation_round_id")
    private InnovationRound innovationRound;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InnovationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Relationships
    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CoInnovation> coInnovations;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FormData> formDataList;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewScore> reviewScores;

    @OneToMany(mappedBy = "innovation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewComment> reviewComments;

    // Pre-persist and pre-update methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InnovationStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum for Innovation Status - Updated according to UML diagram
    public enum InnovationStatus {
        DRAFT, // Bản nháp
        SUBMITTED, // Đã nộp
        PENDING_KHOA_REVIEW, // Chờ Khoa duyệt
        RETURNED_TO_SUBMITTER, // Trả về người nộp
        KHOA_REVIEWED, // Khoa đã duyệt
        KHOA_APPROVED, // Khoa phê duyệt
        KHOA_REJECTED, // Khoa từ chối
        PENDING_TRUONG_REVIEW, // Chờ Trường duyệt
        TRUONG_REVIEWED, // Trường đã duyệt
        TRUONG_APPROVED, // Trường phê duyệt
        TRUONG_REJECTED, // Trường từ chối
        FINAL_APPROVED // Phê duyệt cuối cùng
    }
}