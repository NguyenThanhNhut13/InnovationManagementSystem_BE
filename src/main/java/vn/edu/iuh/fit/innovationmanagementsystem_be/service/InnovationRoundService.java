package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationRoundMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class InnovationRoundService {

    private final InnovationDecisionService innovationDecisionService;
    private final InnovationRoundRepository innovationRoundRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;
    private final InnovationRoundMapper innovationRoundMapper;
    private final InnovationPhaseService innovationPhaseService;

    public InnovationRoundService(InnovationDecisionService innovationDecisionService, InnovationRoundRepository innovationRoundRepository,
                                  InnovationDecisionRepository innovationDecisionRepository,
                                  InnovationRoundMapper innovationRoundMapper, InnovationPhaseService innovationPhaseService) {
        this.innovationDecisionService = innovationDecisionService;
        this.innovationRoundRepository = innovationRoundRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.innovationRoundMapper = innovationRoundMapper;
        this.innovationPhaseService = innovationPhaseService;
    }

    // 1. Create innovation round
    @Transactional
    public InnovationRoundResponse createInnovationRound(CreateInnovationRoundRequest request) {

        // validate
        if (request.getRegistrationStartDate().isAfter(request.getRegistrationEndDate())) {
            throw new IdInvalidException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        // 2. Create entity InnovationRound
        InnovationRound round = new InnovationRound();
        round.setName(request.getName());
        round.setAcademicYear(request.getAcademicYear());
        round.setDescription(request.getDescription());
        round.setRegistrationStartDate(request.getRegistrationStartDate());
        round.setRegistrationEndDate(request.getRegistrationEndDate());
        round.setStatus(request.getStatus());

        // 3. Save round to get ID
        round = innovationRoundRepository.save(round);

        // 4. Create InnovationDecision (call other service)
        if (request.getInnovationDecision() != null) {
            InnovationDecision decisionReq;
            if (request.getInnovationDecision().getId() != null) {
                decisionReq = innovationDecisionService.getEntityById(request.getInnovationDecision().getId())
                        .orElseThrow(() -> new IdInvalidException("Decision không tồn tại"));
            } else {
                decisionReq = innovationDecisionService.createDecision(request.getInnovationDecision());
            }
            round.setInnovationDecision(decisionReq);
        }

        // 5. Create InnovationPhase (call other service)
        if (request.getInnovationPhase() != null && !request.getInnovationPhase().isEmpty()) {
            Set<InnovationPhase> phases = innovationPhaseService.createPhasesForRound(round, request.getInnovationPhase());

            round.setInnovationPhases(phases);
        }

        // 6. Save round with decision + phases
        return innovationRoundMapper.toInnovationRoundResponse(innovationRoundRepository.save(round));
    }

    // 2. Get all rounds by decision with pagination and filtering
    public ResultPaginationDTO getRoundsByDecision(@NonNull String decisionId,
            @NonNull Specification<InnovationRound> specification,
            @NonNull Pageable pageable) {

        // Verify decision exists
        if (!innovationDecisionRepository.existsById(decisionId)) {
            throw new IdInvalidException("Không tìm thấy InnovationDecision với ID: " + decisionId);
        }

        // Create specification that includes decisionId filter
        Specification<InnovationRound> decisionSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(root.get("innovationDecision").get("id"), decisionId);

        // Combine with provided specification
        Specification<InnovationRound> combinedSpec = decisionSpec.and(specification);

        Page<InnovationRound> rounds = innovationRoundRepository.findAll(combinedSpec, pageable);
        Page<InnovationRoundResponse> responses = rounds.map(innovationRoundMapper::toInnovationRoundResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 3. Get round by ID
    public InnovationRoundResponse getRoundById(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));
        return innovationRoundMapper.toInnovationRoundResponse(round);
    }

    // 4. Get current active round
    public InnovationRoundResponse getCurrentActiveRound(String decisionId) {
        InnovationRound round = innovationRoundRepository.findCurrentActiveRound(decisionId, LocalDate.now())
                .orElse(null);
        return round != null ? innovationRoundMapper.toInnovationRoundResponse(round) : null;
    }

    // 5. Update round
    public InnovationRoundResponse updateRound(String roundId, UpdateInnovationRoundRequest request) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        if (request.getName() != null) {
            round.setName(request.getName());
        }
        if (request.getStartDate() != null) {
            round.setRegistrationStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            round.setRegistrationEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            round.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            round.setDescription(request.getDescription());
        }

        if (request.getAcademicYear() != null) {
            round.setAcademicYear(request.getAcademicYear());
        }

        // Validate dates if they were updated
        if (request.getStartDate() != null || request.getEndDate() != null) {
            validateRoundDates(round.getRegistrationStartDate(), round.getRegistrationEndDate());
        }

        InnovationRound savedRound = innovationRoundRepository.save(round);
        return innovationRoundMapper.toInnovationRoundResponse(savedRound);
    }

    // 7. Toggle round status
    public InnovationRoundResponse toggleRoundStatus(String roundId, boolean isActive) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        InnovationRound savedRound = innovationRoundRepository.save(round);
        return innovationRoundMapper.toInnovationRoundResponse(savedRound);
    }

    // 8. Get rounds by status
    public List<InnovationRoundResponse> getRoundsByStatus(String status) {
        List<InnovationRound> rounds = innovationRoundRepository.findByStatus(
                vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum
                        .valueOf(status));
        return rounds.stream()
                .map(innovationRoundMapper::toInnovationRoundResponse)
                .collect(Collectors.toList());
    }

    // 9. Get all innovation rounds with pagination and filtering
    public ResultPaginationDTO getAllInnovationRoundsWithPaginationAndFilter(
            Specification<InnovationRound> specification, Pageable pageable) {
        Page<InnovationRound> roundPage = innovationRoundRepository.findAll(specification, pageable);
        Page<InnovationRoundResponse> responsePage = roundPage.map(innovationRoundMapper::toInnovationRoundResponse);
        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // Helper method to validate round dates
    private void validateRoundDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IdInvalidException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }
}
