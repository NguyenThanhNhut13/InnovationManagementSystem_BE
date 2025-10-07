package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;

import java.util.List;

@Service
@Transactional
public class PhaseTransitionService {

    private final InnovationPhaseRepository innovationPhaseRepository;

    public PhaseTransitionService(InnovationPhaseRepository innovationPhaseRepository) {
        this.innovationPhaseRepository = innovationPhaseRepository;
    }

    // Scheduled job chạy mỗi ngày lúc 00:00 để cập nhật trạng thái phase tự động
    @Scheduled(cron = "0 0 0 * * ?")
    public void updatePhaseStatuses() {

        // 1. Chuyển PENDING → ACTIVE khi đến thời gian bắt đầu
        List<InnovationPhase> pendingPhases = innovationPhaseRepository.findByPhaseStatus(PhaseStatusEnum.PENDING);
        for (InnovationPhase phase : pendingPhases) {
            if (phase.isTimeToStart()) {
                phase.transitionTo(PhaseStatusEnum.ACTIVE, "Tự động chuyển sang hoạt động theo lịch trình");
                innovationPhaseRepository.save(phase);
            }
        }

        // 2. Chuyển ACTIVE → COMPLETED khi hết thời gian
        List<InnovationPhase> activePhases = innovationPhaseRepository.findByPhaseStatus(PhaseStatusEnum.ACTIVE);
        for (InnovationPhase phase : activePhases) {
            if (phase.isTimeToEnd()) {
                phase.transitionTo(PhaseStatusEnum.COMPLETED, "Tự động hoàn thành theo lịch trình");
                innovationPhaseRepository.save(phase);

                // 3. Kích hoạt phase tiếp theo
                // activateNextPhase(phase);
            }
        }
    }

    // Kích hoạt phase tiếp theo trong cùng một round
    // private void activateNextPhase(InnovationPhase completedPhase) {
    // InnovationPhase nextPhase = innovationPhaseRepository
    // .findByInnovationRoundIdAndPhaseOrder(completedPhase.getInnovationRound().getId())
    // .orElse(null);
    //
    // if (nextPhase != null &&
    // PhaseStatusEnum.PENDING.equals(nextPhase.getPhaseStatus())) {
    // // Kiểm tra xem có đến thời gian bắt đầu chưa
    // if (nextPhase.isTimeToStart()) {
    // nextPhase.transitionTo(PhaseStatusEnum.ACTIVE, "Tự động kích hoạt sau khi
    // phase trước hoàn thành");
    // innovationPhaseRepository.save(nextPhase);
    // }
    // }
    // }

    // Chuyển đổi phase thủ công
    public InnovationPhase transitionPhase(String phaseId, PhaseStatusEnum targetStatus, String reason) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phase với ID: " + phaseId));

        // Validate business rules
        validatePhaseTransition(phase, targetStatus);

        phase.transitionTo(targetStatus, reason);
        return innovationPhaseRepository.save(phase);
    }

    // Validate phase transition theo business rules
    private void validatePhaseTransition(InnovationPhase phase, PhaseStatusEnum targetStatus) {
        // 1. Kiểm tra phase có thể chuyển đổi không
        if (!phase.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("Không thể chuyển từ " + phase.getPhaseStatus() + " sang " + targetStatus);
        }

        // // 2. Kiểm tra dependencies - phase trước đó phải hoàn thành
        // if (PhaseStatusEnum.ACTIVE.equals(targetStatus)) {
        // InnovationPhase previousPhase = innovationPhaseRepository
        // .findByInnovationRoundIdAndPhaseOrder(phase.getInnovationRound().getId())
        // .orElse(null);
        //
        // if (previousPhase != null &&
        // !PhaseStatusEnum.COMPLETED.equals(previousPhase.getPhaseStatus())) {
        // throw new IllegalStateException("Phase trước đó chưa hoàn thành, không thể
        // kích hoạt phase này");
        // }
        // }

        // 3. Kiểm tra chỉ có 1 phase ACTIVE tại 1 thời điểm trong cùng round
        if (PhaseStatusEnum.ACTIVE.equals(targetStatus)) {
            List<InnovationPhase> activePhases = innovationPhaseRepository
                    .findByInnovationRoundIdAndPhaseStatus(phase.getInnovationRound().getId(), PhaseStatusEnum.ACTIVE);

            if (!activePhases.isEmpty()) {
                throw new IllegalStateException("Đã có phase đang hoạt động trong round này");
            }
        }
    }

    // Hoàn thành phase thủ công
    public InnovationPhase completePhase(String phaseId, String reason) {
        return transitionPhase(phaseId, PhaseStatusEnum.COMPLETED, reason);
    }

    // Tạm dừng phase
    public InnovationPhase suspendPhase(String phaseId, String reason) {
        return transitionPhase(phaseId, PhaseStatusEnum.SUSPENDED, reason);
    }

    // Hủy phase
    public InnovationPhase cancelPhase(String phaseId, String reason) {
        return transitionPhase(phaseId, PhaseStatusEnum.CANCELLED, reason);
    }
}
