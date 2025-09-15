package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "innovation_phases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InnovationPhase extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "round_start_date", nullable = false)
    private LocalDate roundStartDate;

    @Column(name = "round_end_date", nullable = false)
    private LocalDate roundEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InnovationRoundStatusEnum status;

    // Thông tin giai đoạn cụ thể
    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type", nullable = false)
    private InnovationPhaseEnum phaseType;

    @Column(name = "phase_start_date", nullable = false)
    private LocalDate phaseStartDate;

    @Column(name = "phase_end_date", nullable = false)
    private LocalDate phaseEndDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "phase_order", nullable = false)
    private Integer phaseOrder; // Thứ tự giai đoạn (1, 2, 3, 4)

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_decision_id", nullable = false)
    private InnovationDecision innovationDecision;

    @OneToMany(mappedBy = "innovationPhase", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormTemplate> formTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "innovationPhase", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Innovation> innovations = new ArrayList<>();

    @OneToMany(mappedBy = "innovationPhase", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DepartmentPhase> departmentPhases = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (status == null) {
            status = InnovationRoundStatusEnum.ACTIVE;
        }
    }

    // // Phase methods
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return isActive && !today.isBefore(phaseStartDate) && !today.isAfter(phaseEndDate);
    }

    public boolean isPhaseWithinRoundTimeframe(LocalDate startDate, LocalDate endDate) {
        return !startDate.isBefore(roundStartDate) && !endDate.isAfter(roundEndDate);
    }

    public boolean isPhaseWithinPhaseTimeframe(LocalDate startDate, LocalDate endDate) {
        return !startDate.isBefore(phaseStartDate) && !endDate.isAfter(phaseEndDate);
    }
}
