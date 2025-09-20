package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_status", nullable = false)
    private PhaseStatusEnum phaseStatus = PhaseStatusEnum.PENDING;

    @Column(name = "transition_reason", columnDefinition = "TEXT")
    private String transitionReason; // Lý do chuyển đổi phase

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
        if (phaseStatus == null) {
            phaseStatus = PhaseStatusEnum.PENDING;
        }
    }

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
            this.transitionReason = reason;
        } else {
            throw new IdInvalidException("Không thể chuyển đổi phase từ " + this.phaseStatus + " sang " + targetStatus);
        }
    }
}
