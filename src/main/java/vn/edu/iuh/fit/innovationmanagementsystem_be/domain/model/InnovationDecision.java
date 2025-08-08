package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
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

    @Column(name = "decision_number")
    private String decisionNumber;

    @Column(name = "year_decision")
    private Integer yearDecision;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // Relationships
    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InnovationRound> innovationRounds = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Regulation> regulations = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewScore> reviewScores;

    // Pre-persist and pre-update methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
    }
}