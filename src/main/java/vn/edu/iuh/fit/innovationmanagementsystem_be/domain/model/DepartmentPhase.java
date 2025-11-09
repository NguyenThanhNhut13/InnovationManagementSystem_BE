package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.time.LocalDate;

@Entity
@Table(name = "department_phases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = { "innovationPhase", "department", "innovationRound" })
@ToString(callSuper = true, exclude = { "innovationPhase", "department", "innovationRound" })
public class DepartmentPhase extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phase_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InnovationPhaseTypeEnum phaseType;

    @Column(name = "phase_order", nullable = false)
    private Integer phaseOrder = 0;

    @Column(name = "phase_start_date", nullable = false)
    private LocalDate phaseStartDate;

    @Column(name = "phase_end_date", nullable = false)
    private LocalDate phaseEndDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_status", nullable = false)
    private PhaseStatusEnum phaseStatus = PhaseStatusEnum.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InnovationRoundStatusEnum status = InnovationRoundStatusEnum.DRAFT;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_phase_id", nullable = false)
    private InnovationPhase innovationPhase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_round_id", nullable = false)
    private InnovationRound innovationRound;

}
