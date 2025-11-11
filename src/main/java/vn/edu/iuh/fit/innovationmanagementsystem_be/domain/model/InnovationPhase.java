package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.time.LocalDate;

@Entity
@Table(name = "innovation_phases", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "innovation_round_id", "phase_order" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = { "innovationRound" })
@ToString(callSuper = true, exclude = { "innovationRound" })
public class InnovationPhase extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phase_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InnovationPhaseTypeEnum phaseType;

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
    private InnovationPhaseLevelEnum level;

    @Column(name = "phase_order", nullable = false)
    private Integer phaseOrder = 0;

    @Column(name = "is_deadline", nullable = false)
    private Boolean isDeadline = false;

    @Column(name = "allow_late_submission", nullable = false)
    private Boolean allowLateSubmission = false;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_round_id", nullable = false)
    @JsonIgnore
    private InnovationRound innovationRound;

    // kiểm tra phase có nằm trong thời gian của phase không
    public boolean isPhaseWithinPhaseTimeframe(LocalDate startDate, LocalDate endDate) {
        return !startDate.isBefore(phaseStartDate) && !endDate.isAfter(phaseEndDate);
    }

}
