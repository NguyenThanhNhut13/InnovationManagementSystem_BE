package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "innovation_rounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InnovationRound extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InnovationRoundStatusEnum status;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_decision_id", nullable = false)
    private InnovationDecision innovationDecision;

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormTemplate> formTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Innovation> innovations = new ArrayList<>();

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InnovationPhase> innovationPhases = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = InnovationRoundStatusEnum.ACTIVE;
        }
    }

    // Get current phase
    public InnovationPhase getCurrentPhase() {
        return innovationPhases.stream()
                .filter(InnovationPhase::isCurrentlyActive)
                .findFirst()
                .orElse(null);
    }

    // Get phase by type
    public InnovationPhase getPhaseByType(InnovationPhaseEnum phaseType) {
        return innovationPhases.stream()
                .filter(phase -> phase.getPhaseType() == phaseType)
                .findFirst()
                .orElse(null);
    }

    // Check if there is an active phase
    public boolean hasActivePhase() {
        return innovationPhases.stream().anyMatch(InnovationPhase::isCurrentlyActive);
    }

    // Get completed phases
    public List<InnovationPhase> getCompletedPhases() {
        return innovationPhases.stream()
                .filter(InnovationPhase::isCompleted)
                .sorted((p1, p2) -> p1.getPhaseOrder().compareTo(p2.getPhaseOrder()))
                .toList();
    }

}