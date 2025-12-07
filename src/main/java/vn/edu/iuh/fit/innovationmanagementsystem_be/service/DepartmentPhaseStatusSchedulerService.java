package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service tự động cập nhật trạng thái của DepartmentPhase
 * SCHEDULED -> ACTIVE -> COMPLETED dựa trên thời gian
 */
@Service
@Slf4j
public class DepartmentPhaseStatusSchedulerService {

    private final DepartmentPhaseRepository departmentPhaseRepository;
    private final NotificationService notificationService;
    private final CouncilRepository councilRepository;
    private final CouncilService councilService;

    public DepartmentPhaseStatusSchedulerService(DepartmentPhaseRepository departmentPhaseRepository,
            NotificationService notificationService,
            CouncilRepository councilRepository,
            CouncilService councilService) {
        this.departmentPhaseRepository = departmentPhaseRepository;
        this.notificationService = notificationService;
        this.councilRepository = councilRepository;
        this.councilService = councilService;
    }

    /**
     * Chạy mỗi ngày lúc 00:02 để cập nhật trạng thái department phase
     * Chạy sau InnovationPhase (00:01) để đảm bảo thứ tự
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 2 0 * * ?")
    @Transactional
    public void updateDepartmentPhaseStatuses() {
        log.info("Bắt đầu kiểm tra và cập nhật trạng thái các DepartmentPhase...");

        LocalDate today = LocalDate.now();

        // Cập nhật SCHEDULED -> ACTIVE
        int scheduledToActive = updateScheduledToActive(today);

        // Cập nhật ACTIVE -> COMPLETED
        int activeToCompleted = updateActiveToCompleted(today);

        log.info("Hoàn thành cập nhật trạng thái DepartmentPhase. SCHEDULED->ACTIVE: {}, ACTIVE->COMPLETED: {}",
                scheduledToActive, activeToCompleted);
    }

    /**
     * Cập nhật các department phase từ SCHEDULED sang ACTIVE
     */
    private int updateScheduledToActive(LocalDate currentDate) {
        List<DepartmentPhase> scheduledPhases = departmentPhaseRepository
                .findScheduledDepartmentPhasesReadyToStart(currentDate);

        for (DepartmentPhase phase : scheduledPhases) {
            phase.setPhaseStatus(PhaseStatusEnum.ACTIVE);
            departmentPhaseRepository.save(phase);
            log.info("Cập nhật DepartmentPhase '{}' (ID: {}, Department: {}) từ SCHEDULED sang ACTIVE",
                    phase.getName(), phase.getId(),
                    phase.getDepartment() != null ? phase.getDepartment().getDepartmentName() : "N/A");
        }

        return scheduledPhases.size();
    }

    /**
     * Cập nhật các department phase từ ACTIVE sang COMPLETED
     */
    private int updateActiveToCompleted(LocalDate currentDate) {
        List<DepartmentPhase> activePhases = departmentPhaseRepository
                .findActiveDepartmentPhasesReadyToComplete(currentDate);

        for (DepartmentPhase phase : activePhases) {
            phase.setPhaseStatus(PhaseStatusEnum.COMPLETED);
            departmentPhaseRepository.save(phase);
            log.info("Cập nhật DepartmentPhase '{}' (ID: {}, Department: {}) từ ACTIVE sang COMPLETED",
                    phase.getName(), phase.getId(),
                    phase.getDepartment() != null ? phase.getDepartment().getDepartmentName() : "N/A");

            // Nếu là phase SCORING cấp Khoa, tự động cập nhật trạng thái innovation
            if (phase.getPhaseType() == InnovationPhaseTypeEnum.SCORING) {
                try {
                    String departmentId = phase.getDepartment().getId();
                    String roundId = phase.getInnovationRound().getId();
                    Optional<Council> councilOpt = councilRepository.findByRoundIdAndLevelAndDepartmentId(
                            roundId, ReviewLevelEnum.KHOA, departmentId);

                    if (councilOpt.isPresent()) {
                        Council council = councilOpt.get();
                        councilService.updateInnovationStatusesForCouncil(council);
                        log.info(
                                "Đã tự động cập nhật trạng thái sáng kiến cho council cấp Khoa '{}' (ID: {}) sau khi phase SCORING kết thúc",
                                council.getName(), council.getId());
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi cập nhật trạng thái sáng kiến sau khi phase SCORING cấp Khoa kết thúc: {}",
                            e.getMessage(), e);
                }
            }
        }

        return activePhases.size();
    }

    /**
     * Chạy mỗi ngày lúc 00:03 để gửi thông báo cho giảng viên khi phase bắt đầu
     * Chạy sau task cập nhật status (00:02) để đảm bảo phase đã được chuyển sang
     * ACTIVE
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 3 0 * * ?")
    @Transactional
    public void notifyDepartmentMembersWhenPhaseStarts() {
        log.info("Bắt đầu kiểm tra và gửi thông báo cho giảng viên khi phase bắt đầu...");

        LocalDate today = LocalDate.now();
        List<DepartmentPhase> phasesStartingToday = departmentPhaseRepository
                .findDepartmentPhasesStartingToday(today);

        if (phasesStartingToday.isEmpty()) {
            log.info("Không có phase nào bắt đầu hôm nay");
            return;
        }

        int notificationCount = 0;
        for (DepartmentPhase phase : phasesStartingToday) {
            try {
                notificationService.notifyDepartmentMembersWhenPhaseStarts(phase);
                notificationCount++;
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo cho phase '{}' (ID: {}): {}",
                        phase.getName(), phase.getId(), e.getMessage(), e);
            }
        }

        log.info("Hoàn thành gửi thông báo. Đã gửi thông báo cho {} phase bắt đầu hôm nay",
                notificationCount);
    }

    /**
     * Chạy mỗi ngày lúc 00:04 để kiểm tra và thông báo cho TRUONG_KHOA
     * về việc cần thành lập hội đồng chấm điểm khi phase SUBMISSION sắp hết hạn
     * Chạy sau task thông báo phase bắt đầu (00:03)
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 4 0 * * ?")
    @Transactional
    public void notifyDepartmentManagersToEstablishScoringCommittee() {
        log.info("Bắt đầu kiểm tra và thông báo cho TRUONG_KHOA về việc thành lập hội đồng chấm điểm...");

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.plusDays(3);
        LocalDate endDate = today.plusDays(7);

        List<DepartmentPhase> submissionPhases = departmentPhaseRepository
                .findActiveSubmissionPhasesExpiringSoon(startDate, endDate);

        if (submissionPhases.isEmpty()) {
            log.info("Không có phase SUBMISSION nào sắp hết hạn trong khoảng 3-7 ngày tới");
            return;
        }

        int notificationCount = 0;
        for (DepartmentPhase submissionPhase : submissionPhases) {
            try {
                notificationService.notifyDepartmentManagersToEstablishScoringCommittee(submissionPhase);
                notificationCount++;
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo thành lập hội đồng chấm điểm cho phase '{}' (ID: {}): {}",
                        submissionPhase.getName(), submissionPhase.getId(), e.getMessage(), e);
            }
        }

        log.info("Hoàn thành thông báo. Đã gửi thông báo cho {} phase SUBMISSION sắp hết hạn",
                notificationCount);
    }

    /**
     * Chạy mỗi ngày lúc 00:05 để kiểm tra và thông báo cho TV_HOI_DONG_KHOA
     * về việc chuẩn bị chấm điểm khi phase SCORING sắp bắt đầu
     * Logic: Thông báo vào ngày hôm nay cho các phase SCORING bắt đầu vào ngày mai
     * (tức là thông báo TRƯỚC 1 ngày so với ngày bắt đầu phase)
     * Chạy sau task thông báo thành lập hội đồng (00:04)
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void notifyScoringCommitteeMembersToPrepare() {
        log.info("Bắt đầu kiểm tra và thông báo cho TV_HOI_DONG_KHOA về việc chuẩn bị chấm điểm...");

        LocalDate today = LocalDate.now();
        // Tìm phase SCORING bắt đầu vào ngày mai (thông báo trước 1 ngày)
        LocalDate scoringStartDate = today.plusDays(1);

        List<DepartmentPhase> scoringPhases = departmentPhaseRepository
                .findScoringPhasesStartingTomorrow(scoringStartDate);

        if (scoringPhases.isEmpty()) {
            log.info("Không có phase SCORING nào bắt đầu vào ngày {} (thông báo trước 1 ngày)", scoringStartDate);
            return;
        }

        int notificationCount = 0;
        for (DepartmentPhase scoringPhase : scoringPhases) {
            try {
                notificationService.notifyScoringCommitteeMembersToPrepare(scoringPhase);
                notificationCount++;
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo chuẩn bị chấm điểm cho phase '{}' (ID: {}): {}",
                        scoringPhase.getName(), scoringPhase.getId(), e.getMessage(), e);
            }
        }

        log.info(
                "Hoàn thành thông báo. Đã gửi thông báo cho {} phase SCORING bắt đầu vào ngày {} (thông báo trước 1 ngày)",
                notificationCount, scoringStartDate);
    }

    /**
     * Chạy mỗi ngày lúc 00:06 để thông báo cho TRUONG_KHOA khi:
     * - Phase SUBMISSION kết thúc trong 1 ngày (thông báo trước 1 ngày)
     * - Phase SUBMISSION kết thúc hôm nay (thông báo vào ngày kết thúc)
     * Chạy sau task thông báo chuẩn bị chấm điểm (00:05)
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 6 0 * * ?")
    @Transactional
    public void notifySubmissionPhaseEnding() {
        log.info("Bắt đầu kiểm tra và thông báo cho TRUONG_KHOA về giai đoạn nộp hồ sơ sắp/đã kết thúc...");

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        int notificationCount = 0;

        // 1. Thông báo trước 1 ngày (phase kết thúc vào ngày mai)
        List<DepartmentPhase> phasesEndingTomorrow = departmentPhaseRepository
                .findActiveSubmissionPhasesEndingOn(tomorrow);

        if (!phasesEndingTomorrow.isEmpty()) {
            log.info("Tìm thấy {} phase SUBMISSION kết thúc vào ngày mai ({})",
                    phasesEndingTomorrow.size(), tomorrow);
            for (DepartmentPhase phase : phasesEndingTomorrow) {
                try {
                    notificationService.notifySubmissionPhaseEnding(phase, true);
                    notificationCount++;
                } catch (Exception e) {
                    log.error("Lỗi khi gửi thông báo sắp kết thúc cho phase '{}' (ID: {}): {}",
                            phase.getName(), phase.getId(), e.getMessage(), e);
                }
            }
        } else {
            log.info("Không có phase SUBMISSION nào kết thúc vào ngày mai ({})", tomorrow);
        }

        // 2. Thông báo vào ngày kết thúc (phase kết thúc hôm nay)
        List<DepartmentPhase> phasesEndingToday = departmentPhaseRepository
                .findActiveSubmissionPhasesEndingOn(today);

        if (!phasesEndingToday.isEmpty()) {
            log.info("Tìm thấy {} phase SUBMISSION kết thúc hôm nay ({})",
                    phasesEndingToday.size(), today);
            for (DepartmentPhase phase : phasesEndingToday) {
                try {
                    notificationService.notifySubmissionPhaseEnding(phase, false);
                    notificationCount++;
                } catch (Exception e) {
                    log.error("Lỗi khi gửi thông báo đã kết thúc cho phase '{}' (ID: {}): {}",
                            phase.getName(), phase.getId(), e.getMessage(), e);
                }
            }
        } else {
            log.info("Không có phase SUBMISSION nào kết thúc hôm nay ({})", today);
        }

        log.info("Hoàn thành thông báo. Đã gửi {} thông báo về giai đoạn nộp hồ sơ sắp/đã kết thúc",
                notificationCount);
    }

}
