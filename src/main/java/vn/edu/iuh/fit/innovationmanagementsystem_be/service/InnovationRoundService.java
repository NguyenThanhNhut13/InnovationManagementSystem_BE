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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundListResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import com.fasterxml.jackson.databind.JsonNode;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationRoundMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    public InnovationRoundService(InnovationDecisionService innovationDecisionService,
            InnovationRoundRepository innovationRoundRepository,
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

        // 1. Create entity InnovationRound
        InnovationRound round = new InnovationRound();
        round.setName(request.getName());
        round.setAcademicYear(request.getAcademicYear());
        round.setDescription(request.getDescription());
        round.setRegistrationStartDate(request.getRegistrationStartDate());
        round.setRegistrationEndDate(request.getRegistrationEndDate());
        round.setStatus(request.getStatus());

        // 2. Create InnovationDecision (call other service)
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

        // 3. Save round to get ID
        round = innovationRoundRepository.save(round);

        // 4. Create InnovationPhase (call other service)
        if (request.getInnovationPhase() != null && !request.getInnovationPhase().isEmpty()) {
            Set<InnovationPhase> phases = innovationPhaseService.createPhasesForRound(round,
                    request.getInnovationPhase());

            round.getInnovationPhases().clear();
            round.getInnovationPhases().addAll(phases);
        }

        // 5. Save round with decision + phases
        return innovationRoundMapper.toInnovationRoundResponse(innovationRoundRepository.save(round));
    }

    // 2. Get all rounds by decision with pagination and filtering
    public ResultPaginationDTO getRoundsByDecision(@NonNull String decisionId,
            @NonNull Specification<InnovationRound> specification,
            @NonNull Pageable pageable) {

        if (!innovationDecisionRepository.existsById(decisionId)) {
            throw new IdInvalidException("Không tìm thấy InnovationDecision với ID: " + decisionId);
        }

        Specification<InnovationRound> decisionSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(root.get("innovationDecision").get("id"), decisionId);

        Specification<InnovationRound> combinedSpec = decisionSpec.and(specification);

        Page<InnovationRound> rounds = innovationRoundRepository.findAll(combinedSpec, pageable);
        Page<InnovationRoundResponse> responses = rounds.map(round -> {
            InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
            setStatistics(response, round);
            return response;
        });
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 3. Get round by ID
    public InnovationRoundResponse getRoundById(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));
        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
        setStatistics(response, round);
        return response;
    }

    // 4. Get current active round
    public InnovationRoundResponse getCurrentActiveRound(String decisionId) {
        InnovationRound round = innovationRoundRepository.findCurrentActiveRound(decisionId, LocalDate.now())
                .orElse(null);
        if (round != null) {
            InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
            setStatistics(response, round);
            return response;
        }
        return null;
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

        InnovationRound savedRound = innovationRoundRepository.save(round);
        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(savedRound);
        setStatistics(response, savedRound);
        return response;
    }

    // 7. Toggle round status
    public InnovationRoundResponse toggleRoundStatus(String roundId, boolean isActive) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        InnovationRound savedRound = innovationRoundRepository.save(round);
        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(savedRound);
        setStatistics(response, savedRound);
        return response;
    }

    // 8. Get rounds by status
    public List<InnovationRoundResponse> getRoundsByStatus(String status) {
        List<InnovationRound> rounds = innovationRoundRepository.findByStatus(
                vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum
                        .valueOf(status));
        return rounds.stream()
                .map(round -> {
                    InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
                    setStatistics(response, round);
                    return response;
                })
                .collect(Collectors.toList());
    }

    // 9. Get all innovation rounds with pagination and filtering
    public ResultPaginationDTO getAllInnovationRoundsWithPaginationAndFilter(
            Specification<InnovationRound> specification, Pageable pageable) {
        Page<InnovationRound> roundPage = innovationRoundRepository.findAll(specification, pageable);
        Page<InnovationRoundResponse> responsePage = roundPage.map(round -> {
            InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
            setStatistics(response, round);
            return response;
        });
        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // 10. Get innovation rounds list for table display with pagination and
    // filtering
    public ResultPaginationDTO getInnovationRoundsListForTable(
            Specification<InnovationRound> specification, Pageable pageable) {
        Page<InnovationRound> roundPage = innovationRoundRepository.findAll(specification, pageable);
        Page<InnovationRoundListResponse> responsePage = roundPage.map(this::convertToListResponse);
        return Utils.toResultPaginationDTO(responsePage, pageable);
    }

    // Helper method to convert InnovationRound to InnovationRoundListResponse
    private InnovationRoundListResponse convertToListResponse(InnovationRound round) {
        InnovationRoundListResponse response = new InnovationRoundListResponse();
        response.setId(round.getId());
        response.setName(round.getName());
        response.setAcademicYear(round.getAcademicYear());
        response.setRegistrationStartDate(round.getRegistrationStartDate());
        response.setRegistrationEndDate(round.getRegistrationEndDate());
        response.setStatus(round.getStatus());

        // Count innovation phases
        response.setPhaseCount(round.getInnovationPhases() != null ? round.getInnovationPhases().size() : 0);

        // Count scoring criteria from InnovationDecision
        int criteriaCount = 0;
        if (round.getInnovationDecision() != null && round.getInnovationDecision().getScoringCriteria() != null) {
            JsonNode scoringCriteria = round.getInnovationDecision().getScoringCriteria();
            if (scoringCriteria.isArray()) {
                criteriaCount = scoringCriteria.size();
            } else if (scoringCriteria.isObject()) {
                criteriaCount = scoringCriteria.size();
            }
        }
        response.setCriteriaCount(criteriaCount);

        return response;
    }

    // Helper method to set statistics for InnovationRoundResponse
    private void setStatistics(InnovationRoundResponse response, InnovationRound round) {
        if (round.getInnovations() != null) {
            // Count submissions (innovations with SUBMITTED status)
            int submissionCount = (int) round.getInnovations().stream()
                    .filter(innovation -> innovation.getStatus() == InnovationStatusEnum.SUBMITTED)
                    .count();
            response.setSubmissionCount(submissionCount);

            // Count reviewed innovations (innovations with PENDING_TRUONG_REVIEW status)
            int reviewedCount = (int) round.getInnovations().stream()
                    .filter(innovation -> innovation.getStatus() == InnovationStatusEnum.PENDING_TRUONG_REVIEW)
                    .count();
            response.setReviewedCount(reviewedCount);

            // Count approved innovations (FINAL_APPROVED status)
            int approvedCount = (int) round.getInnovations().stream()
                    .filter(innovation -> innovation.getStatus() == InnovationStatusEnum.FINAL_APPROVED)
                    .count();
            response.setApprovedCount(approvedCount);
        } else {
            response.setSubmissionCount(0);
            response.setReviewedCount(0);
            response.setApprovedCount(0);
        }
    }

    public InnovationRoundResponse getCurrentRound() {
        Optional<InnovationRound> currentRound = innovationRoundRepository.findCurrentActiveRound(LocalDate.now());
        if (currentRound.isEmpty()) {
            return null;
        }

        InnovationRound round = currentRound.get();
        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
        setStatistics(response, round);
        return response;
    }

}
