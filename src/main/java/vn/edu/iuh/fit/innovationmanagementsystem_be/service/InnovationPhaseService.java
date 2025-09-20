package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationPhaseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InnovationPhaseService {

    private final InnovationPhaseRepository innovationPhaseRepository;
    private final InnovationRoundRepository innovationRoundRepository;
    private final InnovationPhaseMapper innovationPhaseMapper;
    private final PhaseTransitionService phaseTransitionService;

    public InnovationPhaseService(InnovationPhaseRepository innovationPhaseRepository,
            InnovationRoundRepository innovationRoundRepository,
            InnovationPhaseMapper innovationPhaseMapper,
            PhaseTransitionService phaseTransitionService) {
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.innovationRoundRepository = innovationRoundRepository;
        this.innovationPhaseMapper = innovationPhaseMapper;
        this.phaseTransitionService = phaseTransitionService;
    }

    // 1. Create phases for an InnovationRound
    public List<InnovationPhaseResponse> createPhasesForRound(String roundId,
            List<InnovationPhaseRequest> phaseRequests) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        // Xóa các giai đoạn cũ nếu có
        innovationPhaseRepository.deleteByInnovationRoundId(roundId);

        // Tạo các giai đoạn mới
        List<InnovationPhase> phases = new ArrayList<>();
        for (InnovationPhaseRequest phaseRequest : phaseRequests) {
            InnovationPhase phase = createPhaseFromRequest(round, phaseRequest);
            phases.add(phase);
        }

        return phases.stream()
                .map(innovationPhaseMapper::toInnovationPhaseResponse)
                .collect(Collectors.toList());
    }

    // 2. Create single phase
    public InnovationPhaseResponse createSinglePhase(String roundId, InnovationPhaseRequest phaseRequest) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        InnovationPhase phase = createPhaseFromRequest(round, phaseRequest);
        return innovationPhaseMapper.toInnovationPhaseResponse(phase);
    }

    // Create phase from request
    private InnovationPhase createPhaseFromRequest(InnovationRound round, InnovationPhaseRequest phaseRequest) {
        // Set default values if not provided
        if (phaseRequest.getName() == null) {
            phaseRequest.setName(round.getName() + " - " + phaseRequest.getPhaseType().name());
        }
        if (phaseRequest.getIsActive() == null) {
            phaseRequest.setIsActive(true);
        }

        InnovationPhase phase = innovationPhaseMapper.toInnovationPhase(phaseRequest);
        phase.setInnovationRound(round);

        // Validate phase dates are within round timeframe
        if (!round.isPhaseWithinRoundTimeframe(phaseRequest.getPhaseStartDate(), phaseRequest.getPhaseEndDate())) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationRound: " +
                    round.getStartDate() + " đến " + round.getEndDate());
        }

        return innovationPhaseRepository.save(phase);
    }

    // 3. Get phases by round
    public List<InnovationPhaseResponse> getPhasesByRound(String roundId) {
        List<InnovationPhase> phases = innovationPhaseRepository.findByInnovationRoundIdOrderByPhaseOrder(roundId);
        return phases.stream()
                .map(innovationPhaseMapper::toInnovationPhaseResponse)
                .collect(Collectors.toList());
    }

    // 4. Get current active phase
    public InnovationPhaseResponse getCurrentActivePhase(String roundId) {
        InnovationPhase phase = innovationPhaseRepository.findCurrentActivePhase(roundId, LocalDate.now())
                .orElse(null);
        return phase != null ? innovationPhaseMapper.toInnovationPhaseResponse(phase) : null;
    }

    // 5. Get phase by type
    public InnovationPhaseResponse getPhaseByType(String roundId, InnovationPhaseEnum phaseType) {
        InnovationPhase phase = innovationPhaseRepository.findByInnovationRoundIdAndPhaseType(roundId, phaseType)
                .orElse(null);
        return phase != null ? innovationPhaseMapper.toInnovationPhaseResponse(phase) : null;
    }

    // 6. Update phase
    public InnovationPhaseResponse updatePhase(String phaseId, UpdateInnovationPhaseRequest request) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        if (request.getName() != null) {
            phase.setName(request.getName());
        }
        if (request.getPhaseType() != null) {
            phase.setPhaseType(request.getPhaseType());
        }
        if (request.getPhaseStartDate() != null) {
            phase.setPhaseStartDate(request.getPhaseStartDate());
        }
        if (request.getPhaseEndDate() != null) {
            phase.setPhaseEndDate(request.getPhaseEndDate());
        }
        if (request.getDescription() != null) {
            phase.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            phase.setIsActive(request.getIsActive());
        }
        if (request.getPhaseOrder() != null) {
            phase.setPhaseOrder(request.getPhaseOrder());
        }

        // Validate phase dates are within round timeframe
        if (request.getPhaseStartDate() != null || request.getPhaseEndDate() != null) {
            if (!phase.getInnovationRound().isPhaseWithinRoundTimeframe(phase.getPhaseStartDate(),
                    phase.getPhaseEndDate())) {
                throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationRound: " +
                        phase.getInnovationRound().getStartDate() + " đến " + phase.getInnovationRound().getEndDate());
            }
        }

        InnovationPhase savedPhase = innovationPhaseRepository.save(phase);
        return innovationPhaseMapper.toInnovationPhaseResponse(savedPhase);
    }

    // 7. Update phase dates
    public InnovationPhaseResponse updatePhaseDates(String phaseId, LocalDate startDate, LocalDate endDate) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        // Validate dates
        validatePhaseDates(startDate, endDate, phase.getInnovationRound());

        phase.setPhaseStartDate(startDate);
        phase.setPhaseEndDate(endDate);

        InnovationPhase savedPhase = innovationPhaseRepository.save(phase);
        return innovationPhaseMapper.toInnovationPhaseResponse(savedPhase);
    }

    // 8. Toggle phase status
    public InnovationPhaseResponse togglePhaseStatus(String phaseId, boolean isActive) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        phase.setIsActive(isActive);
        InnovationPhase savedPhase = innovationPhaseRepository.save(phase);
        return innovationPhaseMapper.toInnovationPhaseResponse(savedPhase);
    }

    // Helper method to validate phase dates
    private void validatePhaseDates(LocalDate startDate, LocalDate endDate, InnovationRound round) {
        if (startDate.isAfter(endDate)) {
            throw new IdInvalidException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        if (!round.isPhaseWithinRoundTimeframe(startDate, endDate)) {
            throw new IdInvalidException("Thời gian giai đoạn phải nằm trong thời gian của InnovationRound: " +
                    round.getStartDate() + " đến " + round.getEndDate());
        }
    }

    // New methods for phase transition
    public InnovationPhaseResponse transitionPhase(String phaseId, PhaseStatusEnum targetStatus, String reason) {
        InnovationPhase phase = phaseTransitionService.transitionPhase(phaseId, targetStatus, reason);
        return innovationPhaseMapper.toInnovationPhaseResponse(phase);
    }

    public InnovationPhaseResponse completePhase(String phaseId, String reason) {
        InnovationPhase phase = phaseTransitionService.completePhase(phaseId, reason);
        return innovationPhaseMapper.toInnovationPhaseResponse(phase);
    }

    public InnovationPhaseResponse suspendPhase(String phaseId, String reason) {
        InnovationPhase phase = phaseTransitionService.suspendPhase(phaseId, reason);
        return innovationPhaseMapper.toInnovationPhaseResponse(phase);
    }

    public InnovationPhaseResponse cancelPhase(String phaseId, String reason) {
        InnovationPhase phase = phaseTransitionService.cancelPhase(phaseId, reason);
        return innovationPhaseMapper.toInnovationPhaseResponse(phase);
    }

    public List<InnovationPhaseResponse> getPhasesByStatus(String roundId, PhaseStatusEnum status) {
        List<InnovationPhase> phases = innovationPhaseRepository.findByRoundIdAndStatusOrderByPhaseOrder(roundId,
                status);
        return phases.stream()
                .map(innovationPhaseMapper::toInnovationPhaseResponse)
                .collect(Collectors.toList());
    }

    public Object getPhaseStatusSummary(String roundId) {
        List<InnovationPhase> allPhases = innovationPhaseRepository.findByInnovationRoundIdOrderByPhaseOrder(roundId);

        return allPhases.stream()
                .collect(Collectors.groupingBy(
                        InnovationPhase::getPhaseStatus,
                        Collectors.counting()));
    }
}