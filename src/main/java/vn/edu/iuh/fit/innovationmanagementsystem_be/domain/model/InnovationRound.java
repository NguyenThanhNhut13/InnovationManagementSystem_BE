package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "innovation_rounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InnovationRound {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InnovationRoundStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Relationships
    @OneToOne(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InnovationDecision innovationDecision;

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FormTemplate> formTemplates;

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Innovation> innovations;

    // Pre-persist and pre-update methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InnovationRoundStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum for Innovation Round Status
    public enum InnovationRoundStatus {
        ACTIVE,
        INACTIVE,
        CLOSED
    }
}