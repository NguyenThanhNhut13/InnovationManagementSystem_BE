package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service tự động cập nhật trạng thái của InnovationPhase
 * SCHEDULED -> ACTIVE -> COMPLETED dựa trên thời gian
 */
@Service
@Slf4j
public class PhaseStatusSchedulerService {

    private final InnovationPhaseRepository innovationPhaseRepository;
    private final CouncilRepository councilRepository;
    private final CouncilService councilService;

    public PhaseStatusSchedulerService(InnovationPhaseRepository innovationPhaseRepository,
            CouncilRepository councilRepository,
            CouncilService councilService) {
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.councilRepository = councilRepository;
        this.councilService = councilService;
    }

    /**
     * Chạy mỗi ngày lúc 00:01 để cập nhật trạng thái phase
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void updatePhaseStatuses() {
        log.info("Bắt đầu kiểm tra và cập nhật trạng thái các InnovationPhase...");

        LocalDate today = LocalDate.now();

        // Cập nhật SCHEDULED -> ACTIVE
        int scheduledToActive = updateScheduledToActive(today);

        // Cập nhật ACTIVE -> COMPLETED
        int activeToCompleted = updateActiveToCompleted(today);

        log.info("Hoàn thành cập nhật trạng thái phase. SCHEDULED->ACTIVE: {}, ACTIVE->COMPLETED: {}",
                scheduledToActive, activeToCompleted);
    }

    /**
     * Cập nhật các phase từ SCHEDULED sang ACTIVE
     */
    private int updateScheduledToActive(LocalDate currentDate) {
        List<InnovationPhase> scheduledPhases = innovationPhaseRepository.findScheduledPhasesReadyToStart(currentDate);

        for (InnovationPhase phase : scheduledPhases) {
            phase.setPhaseStatus(PhaseStatusEnum.ACTIVE);
            innovationPhaseRepository.save(phase);
            log.info("Cập nhật phase '{}' (ID: {}) từ SCHEDULED sang ACTIVE",
                    phase.getName(), phase.getId());
        }

        return scheduledPhases.size();
    }

    /**
     * Cập nhật các phase từ ACTIVE sang COMPLETED
     */
    private int updateActiveToCompleted(LocalDate currentDate) {
        List<InnovationPhase> activePhases = innovationPhaseRepository.findActivePhasesReadyToComplete(currentDate);

        for (InnovationPhase phase : activePhases) {
            phase.setPhaseStatus(PhaseStatusEnum.COMPLETED);
            innovationPhaseRepository.save(phase);
            log.info("Cập nhật phase '{}' (ID: {}) từ ACTIVE sang COMPLETED",
                    phase.getName(), phase.getId());

            // Nếu là phase SCORING cấp Trường, tự động cập nhật trạng thái innovation
            if (phase.getPhaseType() == InnovationPhaseTypeEnum.SCORING
                    && phase.getLevel() == InnovationPhaseLevelEnum.SCHOOL) {
                try {
                    String roundId = phase.getInnovationRound().getId();
                    Optional<Council> councilOpt = councilRepository.findByRoundIdAndLevelAndNoDepartment(
                            roundId, ReviewLevelEnum.TRUONG);

                    if (councilOpt.isPresent()) {
                        Council council = councilOpt.get();
                        councilService.updateInnovationStatusesForCouncil(council);
                        log.info("Đã tự động cập nhật trạng thái sáng kiến cho council cấp Trường '{}' (ID: {}) sau khi phase SCORING kết thúc",
                                council.getName(), council.getId());
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi cập nhật trạng thái sáng kiến sau khi phase SCORING cấp Trường kết thúc: {}",
                            e.getMessage(), e);
                }
            }
        }

        return activePhases.size();
    }

}
