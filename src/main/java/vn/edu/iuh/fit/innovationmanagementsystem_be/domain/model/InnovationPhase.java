package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

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

    // Thông tin giai đoạn cụ thể
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
    private PhaseStatusEnum phaseStatus = PhaseStatusEnum.PENDING;

    @Enumerated(EnumType.STRING)
    private InnovationPhaseLevelEnum level;

    @Column(name = "phase_order", nullable = false)
    private Integer phaseOrder = 0;

    @Column(name = "is_deadline", nullable = false)
    private Boolean isDeadline = false;

    // @Column(name = "transition_reason", columnDefinition = "TEXT")
    // private String transitionReason; // Lý do chuyển đổi phase

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_round_id", nullable = false)
    @JsonIgnore
    private InnovationRound innovationRound;

    // kiểm tra phase có nằm trong thời gian của round không
    public boolean isPhaseWithinRoundTimeframe(LocalDate startDate, LocalDate endDate) {
        return innovationRound != null &&
                innovationRound.isPhaseWithinRoundTimeframe(startDate, endDate);
    }

    // kiểm tra phase có nằm trong thời gian của phase không
    public boolean isPhaseWithinPhaseTimeframe(LocalDate startDate, LocalDate endDate) {
        return !startDate.isBefore(phaseStartDate) && !endDate.isAfter(phaseEndDate);
    }

    // kiểm tra phase có thể chuyển đổi trạng thái không
    public boolean canTransitionTo(PhaseStatusEnum targetStatus) {
        return PhaseStatusEnum.PENDING.equals(this.phaseStatus) &&
                (PhaseStatusEnum.ACTIVE.equals(targetStatus) || PhaseStatusEnum.CANCELLED.equals(targetStatus)) ||
                PhaseStatusEnum.ACTIVE.equals(this.phaseStatus) &&
                        (PhaseStatusEnum.COMPLETED.equals(targetStatus)
                                || PhaseStatusEnum.SUSPENDED.equals(targetStatus)
                                || PhaseStatusEnum.CANCELLED.equals(targetStatus))
                ||
                PhaseStatusEnum.SUSPENDED.equals(this.phaseStatus) &&
                        (PhaseStatusEnum.ACTIVE.equals(targetStatus) || PhaseStatusEnum.CANCELLED.equals(targetStatus));
    }

    // kiểm tra phase có đến thời gian bắt đầu không
    public boolean isTimeToStart() {
        return PhaseStatusEnum.PENDING.equals(this.phaseStatus) &&
                LocalDate.now().isAfter(phaseStartDate) || LocalDate.now().equals(phaseStartDate);
    }

    // kiểm tra phase có đến thời gian kết thúc không
    public boolean isTimeToEnd() {
        return PhaseStatusEnum.ACTIVE.equals(this.phaseStatus) &&
                LocalDate.now().isAfter(phaseEndDate);
    }

    // chuyển đổi trạng thái của phase
    public void transitionTo(PhaseStatusEnum targetStatus, String reason) {
        if (canTransitionTo(targetStatus)) {
            this.phaseStatus = targetStatus;
        } else {
            throw new IdInvalidException("Không thể chuyển đổi phase từ " + this.phaseStatus + " sang " + targetStatus);
        }
    }
}
