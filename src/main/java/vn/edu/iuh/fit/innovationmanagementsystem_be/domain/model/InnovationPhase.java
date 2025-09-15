package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;

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
    @JoinColumn(name = "innovation_round_id", nullable = false)
    private InnovationRound innovationRound;

    @OneToMany(mappedBy = "innovationPhase", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DepartmentPhase> departmentPhases = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
    }

    public boolean isPhaseWithinRoundTimeframe(LocalDate startDate, LocalDate endDate) {
        return innovationRound != null &&
                innovationRound.isPhaseWithinRoundTimeframe(startDate, endDate);
    }

    public boolean isPhaseWithinPhaseTimeframe(LocalDate startDate, LocalDate endDate) {
        return !startDate.isBefore(phaseStartDate) && !endDate.isAfter(phaseEndDate);
    }
}
