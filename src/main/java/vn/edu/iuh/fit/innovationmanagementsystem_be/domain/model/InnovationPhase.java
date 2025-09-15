package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;

import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type", nullable = false)
    private InnovationPhaseEnum phaseType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

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

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
    }

    /**
     * Kiểm tra xem giai đoạn có đang diễn ra không
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return isActive && !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Kiểm tra xem giai đoạn đã kết thúc chưa
     */
    public boolean isCompleted() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Kiểm tra xem giai đoạn chưa bắt đầu
     */
    public boolean isNotStarted() {
        return LocalDate.now().isBefore(startDate);
    }
}
