package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateRoundPhasesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationPhaseResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationPhaseMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InnovationPhaseService {

    private final InnovationPhaseRepository innovationPhaseRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;
    private final InnovationPhaseMapper innovationPhaseMapper;

    public InnovationPhaseService(InnovationPhaseRepository innovationPhaseRepository,
            InnovationDecisionRepository innovationDecisionRepository,
            InnovationPhaseMapper innovationPhaseMapper) {
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.innovationPhaseMapper = innovationPhaseMapper;
    }

    // 1. Create phases with custom dates for an InnovationDecision
    public List<InnovationPhaseResponse> createPhasesForDecision(CreateRoundPhasesRequest request) {
        InnovationDecision decision = innovationDecisionRepository.findById(request.getDecisionId())
                .orElseThrow(
                        () -> new IdInvalidException(
                                "Không tìm thấy InnovationDecision với ID: " + request.getDecisionId()));

        // Xóa các giai đoạn cũ nếu có
        innovationPhaseRepository.deleteByInnovationDecisionId(request.getDecisionId());

        // Tạo các giai đoạn mới với thời gian tùy chỉnh
        List<InnovationPhase> phases = new ArrayList<>();
        for (InnovationPhaseRequest phaseRequest : request.getPhases()) {
            InnovationPhase phase = createPhaseFromRequest(decision, phaseRequest, request);
            phases.add(phase);
        }

        return phases.stream()
                .map(innovationPhaseMapper::toInnovationPhaseResponse)
                .collect(Collectors.toList());
    }

    // Create phase from request
    private InnovationPhase createPhaseFromRequest(InnovationDecision decision, InnovationPhaseRequest phaseRequest,
            CreateRoundPhasesRequest roundRequest) {
        // Set default values if not provided
        if (phaseRequest.getName() == null) {
            phaseRequest.setName(roundRequest.getRoundName());
        }
        if (phaseRequest.getStatus() == null) {
            phaseRequest.setStatus(
                    roundRequest.getStatus() != null ? roundRequest.getStatus() : InnovationRoundStatusEnum.ACTIVE);
        }
        if (phaseRequest.getIsActive() == null) {
            phaseRequest.setIsActive(true);
        }

        // Use mapper to convert request to entity
        InnovationPhase phase = innovationPhaseMapper.toInnovationPhase(phaseRequest);
        phase.setInnovationDecision(decision);

        // Set round information from the main request
        phase.setRoundStartDate(roundRequest.getRoundStartDate());
        phase.setRoundEndDate(roundRequest.getRoundEndDate());

        return innovationPhaseRepository.save(phase);
    }

    // 2. Create single phase
    public InnovationPhaseResponse createSinglePhase(String decisionId, InnovationPhaseRequest phaseRequest) {
        InnovationDecision decision = innovationDecisionRepository.findById(decisionId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationDecision với ID: " + decisionId));

        // Create a temporary CreateRoundPhasesRequest for single phase creation
        CreateRoundPhasesRequest tempRequest = new CreateRoundPhasesRequest();
        tempRequest.setDecisionId(decisionId);
        tempRequest.setRoundName(decision.getTitle());
        // Use default dates for single phase creation
        tempRequest.setRoundStartDate(LocalDate.now());
        tempRequest.setRoundEndDate(LocalDate.now().plusMonths(3));
        tempRequest.setStatus(InnovationRoundStatusEnum.ACTIVE);

        InnovationPhase phase = createPhaseFromRequest(decision, phaseRequest, tempRequest);
        return innovationPhaseMapper.toInnovationPhaseResponse(phase);
    }

    // 3. Get current phase
    public InnovationPhaseResponse getCurrentPhase(String decisionId) {
        // Validate decision exists
        innovationDecisionRepository.findById(decisionId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationDecision với ID: " + decisionId));

        InnovationPhase phase = innovationPhaseRepository.findByInnovationDecisionIdOrderByPhaseOrder(decisionId)
                .stream()
                .filter(InnovationPhase::isCurrentlyActive)
                .findFirst()
                .orElse(null);

        return phase != null ? innovationPhaseMapper.toInnovationPhaseResponse(phase) : null;
    }

    // 4. Get phases by decision id
    public List<InnovationPhaseResponse> getPhasesByDecisionId(String decisionId) {
        List<InnovationPhase> phases = innovationPhaseRepository
                .findByInnovationDecisionIdOrderByPhaseOrder(decisionId);
        return phases.stream()
                .map(innovationPhaseMapper::toInnovationPhaseResponse)
                .collect(Collectors.toList());
    }

    // 5. Get phase by type
    public InnovationPhaseResponse getPhaseByType(String decisionId, InnovationPhaseEnum phaseType) {
        // Validate decision exists
        innovationDecisionRepository.findById(decisionId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationDecision với ID: " + decisionId));

        InnovationPhase phase = innovationPhaseRepository.findByInnovationDecisionIdAndPhaseType(decisionId, phaseType)
                .orElse(null);
        return phase != null ? innovationPhaseMapper.toInnovationPhaseResponse(phase) : null;
    }

    // 6. Update phase dates
    public InnovationPhaseResponse updatePhaseDates(String phaseId, LocalDate startDate, LocalDate endDate) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        phase.setPhaseStartDate(startDate);
        phase.setPhaseEndDate(endDate);

        InnovationPhase savedPhase = innovationPhaseRepository.save(phase);
        return innovationPhaseMapper.toInnovationPhaseResponse(savedPhase);
    }

    // 7. Update phase status
    public InnovationPhaseResponse togglePhaseStatus(String phaseId, boolean isActive) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        phase.setIsActive(isActive);

        InnovationPhase savedPhase = innovationPhaseRepository.save(phase);
        return innovationPhaseMapper.toInnovationPhaseResponse(savedPhase);
    }

    // 8. Update phase
    public InnovationPhaseResponse updatePhase(String phaseId, UpdateInnovationPhaseRequest request) {
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + phaseId));

        // Update only non-null fields
        if (request.getName() != null) {
            phase.setName(request.getName());
        }
        if (request.getRoundStartDate() != null) {
            phase.setRoundStartDate(request.getRoundStartDate());
        }
        if (request.getRoundEndDate() != null) {
            phase.setRoundEndDate(request.getRoundEndDate());
        }
        if (request.getStatus() != null) {
            phase.setStatus(request.getStatus());
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

        InnovationPhase savedPhase = innovationPhaseRepository.save(phase);
        return innovationPhaseMapper.toInnovationPhaseResponse(savedPhase);
    }

    // 9. Check if can submit innovation
    public boolean canSubmitInnovation(String decisionId) {
        InnovationPhaseResponse submissionPhase = getPhaseByType(decisionId, InnovationPhaseEnum.SUBMISSION);
        if (submissionPhase == null) {
            return false;
        }

        // Get the actual entity to check if currently active
        InnovationPhase phase = innovationPhaseRepository.findById(submissionPhase.getId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy InnovationPhase với ID: " + submissionPhase.getId()));

        return phase.isCurrentlyActive();
    }

    // 10. Check if can review at department level
    public boolean canReviewAtDepartmentLevel(String decisionId) {
        InnovationPhaseResponse reviewPhase = getPhaseByType(decisionId, InnovationPhaseEnum.DEPARTMENT_REVIEW);
        if (reviewPhase == null) {
            return false;
        }

        InnovationPhase phase = innovationPhaseRepository.findById(reviewPhase.getId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + reviewPhase.getId()));

        return phase.isCurrentlyActive();
    }

    // 11. Check if can review at university level
    public boolean canReviewAtUniversityLevel(String decisionId) {
        InnovationPhaseResponse reviewPhase = getPhaseByType(decisionId, InnovationPhaseEnum.UNIVERSITY_REVIEW);
        if (reviewPhase == null) {
            return false;
        }

        InnovationPhase phase = innovationPhaseRepository.findById(reviewPhase.getId())
                .orElseThrow(
                        () -> new IdInvalidException("Không tìm thấy InnovationPhase với ID: " + reviewPhase.getId()));

        return phase.isCurrentlyActive();
    }

    // 12. Check if can announce results
    public boolean canAnnounceResults(String decisionId) {
        InnovationPhaseResponse announcementPhase = getPhaseByType(decisionId, InnovationPhaseEnum.RESULT_ANNOUNCEMENT);
        if (announcementPhase == null) {
            return false;
        }

        InnovationPhase phase = innovationPhaseRepository.findById(announcementPhase.getId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy InnovationPhase với ID: " + announcementPhase.getId()));

        return phase.isCurrentlyActive();
    }

}
