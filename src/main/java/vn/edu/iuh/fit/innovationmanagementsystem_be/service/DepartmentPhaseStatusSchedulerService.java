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

    public DepartmentPhaseStatusSchedulerService(DepartmentPhaseRepository departmentPhaseRepository) {
        this.departmentPhaseRepository = departmentPhaseRepository;
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

}
