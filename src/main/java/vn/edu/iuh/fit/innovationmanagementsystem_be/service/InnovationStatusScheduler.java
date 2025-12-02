package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Service tự động cập nhật trạng thái innovation
 * khi giai đoạn SCORING của department bắt đầu ACTIVE
 */
@Service
@Slf4j
public class InnovationStatusScheduler {

    private final DepartmentPhaseRepository departmentPhaseRepository;
    private final InnovationRepository innovationRepository;

    public InnovationStatusScheduler(DepartmentPhaseRepository departmentPhaseRepository,
            InnovationRepository innovationRepository) {
        this.departmentPhaseRepository = departmentPhaseRepository;
        this.innovationRepository = innovationRepository;
    }

    /**
     * Chạy mỗi ngày lúc 00:07 để cập nhật status innovation
     * khi phase SCORING cấp Khoa bắt đầu ACTIVE
     * Chạy sau các scheduled job khác để đảm bảo phase đã được cập nhật
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 7 0 * * ?")
    @Transactional
    public void updateInnovationStatusWhenScoringStarts() {
        log.info("Bắt đầu kiểm tra và cập nhật status innovation khi SCORING cấp Khoa bắt đầu...");

        LocalDate today = LocalDate.now();
        int totalUpdated = 0;

        // Xử lý phase SCORING cấp Khoa (DepartmentPhase): SUBMITTED → PENDING_KHOA_REVIEW
        List<DepartmentPhase> scoringPhases = departmentPhaseRepository.findScoringPhasesActivatedToday(today);

        for (DepartmentPhase scoringPhase : scoringPhases) {
            try {
                String departmentId = scoringPhase.getDepartment().getId();
                String roundId = scoringPhase.getInnovationRound().getId();
                String departmentName = scoringPhase.getDepartment().getDepartmentName();

                // Tìm innovations có status SUBMITTED
                List<Innovation> innovations = innovationRepository
                        .findByDepartmentIdAndRoundIdAndStatus(
                                departmentId,
                                roundId,
                                InnovationStatusEnum.SUBMITTED);

                if (innovations.isEmpty()) {
                    log.info("Không có innovation SUBMITTED nào của department '{}' trong round này",
                            departmentName);
                    continue;
                }

                // Chuyển status sang PENDING_KHOA_REVIEW
                for (Innovation innovation : innovations) {
                    innovation.setStatus(InnovationStatusEnum.PENDING_KHOA_REVIEW);
                    innovationRepository.save(innovation);
                    totalUpdated++;

                    log.info("Cập nhật innovation '{}' (ID: {}) sang PENDING_KHOA_REVIEW",
                            innovation.getInnovationName(), innovation.getId());
                }

                log.info("Đã cập nhật {} innovation của department '{}' sang PENDING_KHOA_REVIEW",
                        innovations.size(), departmentName);

            } catch (Exception e) {
                log.error("Lỗi khi cập nhật innovation cho phase '{}' (ID: {}): {}",
                        scoringPhase.getName(), scoringPhase.getId(), e.getMessage(), e);
            }
        }

        log.info("Hoàn thành cập nhật status innovation khi SCORING cấp Khoa bắt đầu. Tổng số innovation đã cập nhật: {}",
                totalUpdated);
    }

}
