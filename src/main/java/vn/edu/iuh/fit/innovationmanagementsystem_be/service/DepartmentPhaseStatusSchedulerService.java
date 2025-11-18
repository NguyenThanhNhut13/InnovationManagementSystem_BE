package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Service tự động cập nhật trạng thái của DepartmentPhase
 * SCHEDULED -> ACTIVE -> COMPLETED dựa trên thời gian
 */
@Service
@Slf4j
public class DepartmentPhaseStatusSchedulerService {

    private final DepartmentPhaseRepository departmentPhaseRepository;
    private final NotificationService notificationService;

    public DepartmentPhaseStatusSchedulerService(DepartmentPhaseRepository departmentPhaseRepository,
            NotificationService notificationService) {
        this.departmentPhaseRepository = departmentPhaseRepository;
        this.notificationService = notificationService;
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

}
