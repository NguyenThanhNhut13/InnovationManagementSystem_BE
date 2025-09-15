package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateRoundPhasesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InnovationPhaseService {

    private final InnovationPhaseRepository innovationPhaseRepository;
    private final InnovationRoundRepository innovationRoundRepository;

    public InnovationPhaseService(InnovationPhaseRepository innovationPhaseRepository,
            InnovationRoundRepository innovationRoundRepository) {
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.innovationRoundRepository = innovationRoundRepository;
    }

    // 1. Create phases with custom dates for a round
    public List<InnovationPhase> createPhasesForRound(CreateRoundPhasesRequest request) {
        InnovationRound round = innovationRoundRepository.findById(request.getRoundId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + request.getRoundId()));

        // Xóa các giai đoạn cũ nếu có
        innovationPhaseRepository.deleteByInnovationRoundId(request.getRoundId());

        // Tạo các giai đoạn mới với thời gian tùy chỉnh
        List<InnovationPhase> phases = new ArrayList<>();
        for (InnovationPhaseRequest phaseRequest : request.getPhases()) {
            InnovationPhase phase = createPhaseFromRequest(round, phaseRequest);
            phases.add(phase);
        }

        return phases;
    }

    // Create phase from request
    private InnovationPhase createPhaseFromRequest(InnovationRound round, InnovationPhaseRequest phaseRequest) {
        InnovationPhase phase = new InnovationPhase();
        phase.setPhaseType(phaseRequest.getPhaseType());
        phase.setStartDate(phaseRequest.getStartDate());
        phase.setEndDate(phaseRequest.getEndDate());
        phase.setDescription(phaseRequest.getDescription());
        phase.setPhaseOrder(phaseRequest.getPhaseOrder());
        phase.setInnovationRound(round);
        phase.setIsActive(true);

        return innovationPhaseRepository.save(phase);
    }

    // 2. Create phase
    public InnovationPhase createPhase(InnovationRound round, InnovationPhaseEnum phaseType,
            Integer phaseOrder, LocalDate startDate, LocalDate endDate,
            String description) {
        InnovationPhase phase = new InnovationPhase();
        phase.setPhaseType(phaseType);
        phase.setStartDate(startDate);
        phase.setEndDate(endDate);
        phase.setDescription(description);
        phase.setPhaseOrder(phaseOrder);
        phase.setInnovationRound(round);
        phase.setIsActive(true);

        return innovationPhaseRepository.save(phase);
    }

    // 3. Create single phase
    public InnovationPhase createSinglePhase(String roundId, InnovationPhaseRequest phaseRequest) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        return createPhaseFromRequest(round, phaseRequest);
    }

    // 4. Get current phase
    public InnovationPhase getCurrentPhase(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        return round.getCurrentPhase();
    }

    // 5. Get phases by round id
    public List<InnovationPhase> getPhasesByRoundId(String roundId) {
        return innovationPhaseRepository.findByInnovationRoundIdOrderByPhaseOrder(roundId);
    }

    // 6. Get phase by type
    public InnovationPhase getPhaseByType(String roundId, InnovationPhaseEnum phaseType) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        return round.getPhaseByType(phaseType);
    }

    // 7. Update phase dates
    public InnovationPhase updatePhaseDates(String phaseId, LocalDate startDate, LocalDate endDate) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        phase.setStartDate(startDate);
        phase.setEndDate(endDate);

        return innovationPhaseRepository.save(phase);
    }

    // 8. Update phase status
    public InnovationPhase togglePhaseStatus(String phaseId, boolean isActive) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        phase.setIsActive(isActive);

        return innovationPhaseRepository.save(phase);
    }

    // 9. Check if can submit innovation
    public boolean canSubmitInnovation(String roundId) {
        InnovationPhase submissionPhase = getPhaseByType(roundId, InnovationPhaseEnum.SUBMISSION);
        return submissionPhase != null && submissionPhase.isCurrentlyActive();
    }

    // 10. Check if can review at department level
    public boolean canReviewAtDepartmentLevel(String roundId) {
        InnovationPhase reviewPhase = getPhaseByType(roundId, InnovationPhaseEnum.DEPARTMENT_REVIEW);
        return reviewPhase != null && reviewPhase.isCurrentlyActive();
    }

    // 11. Check if can review at university level
    public boolean canReviewAtUniversityLevel(String roundId) {
        InnovationPhase reviewPhase = getPhaseByType(roundId, InnovationPhaseEnum.UNIVERSITY_REVIEW);
        return reviewPhase != null && reviewPhase.isCurrentlyActive();
    }

    // 12. Check if can announce results
    public boolean canAnnounceResults(String roundId) {
        InnovationPhase announcementPhase = getPhaseByType(roundId, InnovationPhaseEnum.RESULT_ANNOUNCEMENT);
        return announcementPhase != null && announcementPhase.isCurrentlyActive();
    }
}
