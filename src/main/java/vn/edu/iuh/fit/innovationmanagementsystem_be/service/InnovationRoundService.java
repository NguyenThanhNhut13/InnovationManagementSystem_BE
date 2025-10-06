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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundListResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import com.fasterxml.jackson.databind.JsonNode;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationRoundMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class InnovationRoundService {

    private final InnovationDecisionService innovationDecisionService;
    private final InnovationRoundRepository innovationRoundRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;
    private final InnovationPhaseRepository innovationPhaseRepository;
    private final InnovationRoundMapper innovationRoundMapper;
    private final InnovationPhaseService innovationPhaseService;

    public InnovationRoundService(InnovationDecisionService innovationDecisionService,
            InnovationRoundRepository innovationRoundRepository,
            InnovationDecisionRepository innovationDecisionRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            InnovationRoundMapper innovationRoundMapper, InnovationPhaseService innovationPhaseService) {
        this.innovationDecisionService = innovationDecisionService;
        this.innovationRoundRepository = innovationRoundRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
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

        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
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
    public InnovationRoundResponse updateRound(String roundId, CreateInnovationRoundRequest request) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        boolean hasChanges = false;

        // Update basic round information
        if (request.getName() != null && !request.getName().equals(round.getName())) {
            round.setName(request.getName());
            hasChanges = true;
        }
        if (request.getRegistrationStartDate() != null
                && !request.getRegistrationStartDate().equals(round.getRegistrationStartDate())) {
            round.setRegistrationStartDate(request.getRegistrationStartDate());
            hasChanges = true;
        }
        if (request.getRegistrationEndDate() != null
                && !request.getRegistrationEndDate().equals(round.getRegistrationEndDate())) {
            round.setRegistrationEndDate(request.getRegistrationEndDate());
            hasChanges = true;
        }
        if (request.getStatus() != null && !request.getStatus().equals(round.getStatus())) {
            round.setStatus(request.getStatus());
            hasChanges = true;
        }
        if (request.getDescription() != null && !request.getDescription().equals(round.getDescription())) {
            round.setDescription(request.getDescription());
            hasChanges = true;
        }
        if (request.getAcademicYear() != null && !request.getAcademicYear().equals(round.getAcademicYear())) {
            round.setAcademicYear(request.getAcademicYear());
            hasChanges = true;
        }

        // Update InnovationDecision
        if (request.getInnovationDecision() != null) {
            if (round.getInnovationDecision() != null) {
                // Update existing decisio
                InnovationDecision existingDecision = round.getInnovationDecision();
                boolean decisionHasChanges = false;

                // Update decision fields
                if (request.getInnovationDecision().getDecisionNumber() != null
                        && !request.getInnovationDecision().getDecisionNumber()
                                .equals(existingDecision.getDecisionNumber())) {
                    existingDecision.setDecisionNumber(request.getInnovationDecision().getDecisionNumber());
                    decisionHasChanges = true;
                }
                if (request.getInnovationDecision().getTitle() != null
                        && !request.getInnovationDecision().getTitle().equals(existingDecision.getTitle())) {
                    existingDecision.setTitle(request.getInnovationDecision().getTitle());
                    decisionHasChanges = true;
                }
                if (request.getInnovationDecision().getPromulgatedDate() != null
                        && !request.getInnovationDecision().getPromulgatedDate()
                                .equals(existingDecision.getPromulgatedDate())) {
                    existingDecision.setPromulgatedDate(request.getInnovationDecision().getPromulgatedDate());
                    decisionHasChanges = true;
                }
                if (request.getInnovationDecision().getFileName() != null
                        && !request.getInnovationDecision().getFileName().equals(existingDecision.getFileName())) {
                    existingDecision.setFileName(request.getInnovationDecision().getFileName());
                    decisionHasChanges = true;
                }
                if (request.getInnovationDecision().getScoringCriteria() != null
                        && !request.getInnovationDecision().getScoringCriteria()
                                .equals(existingDecision.getScoringCriteria())) {
                    existingDecision.setScoringCriteria(request.getInnovationDecision().getScoringCriteria());
                    decisionHasChanges = true;
                }
                if (request.getInnovationDecision().getContentGuide() != null
                        && !request.getInnovationDecision().getContentGuide()
                                .equals(existingDecision.getContentGuide())) {
                    existingDecision.setContentGuide(request.getInnovationDecision().getContentGuide());
                    decisionHasChanges = true;
                }

                if (decisionHasChanges) {
                    innovationDecisionRepository.save(existingDecision);
                    hasChanges = true;
                }
            } else {
                // Create new decision if round doesn't have one
                InnovationDecision decisionReq = innovationDecisionService
                        .createDecision(request.getInnovationDecision());
                round.setInnovationDecision(decisionReq);
                hasChanges = true;
            }
        }

        // Update InnovationPhase
        if (request.getInnovationPhase() != null && !request.getInnovationPhase().isEmpty()) {

            boolean phasesChanged = checkPhasesChanged(round, new ArrayList<>(request.getInnovationPhase()));
            if (phasesChanged) {
                // Save round to get ID before updating phases
                if (hasChanges) {
                    round = innovationRoundRepository.save(round);
                }
                // Update existing phases or create new ones
                updatePhasesForRound(round, new ArrayList<>(request.getInnovationPhase()));
                hasChanges = true;
            }
        }

        // Only save if there are actual changes
        InnovationRound savedRound;
        if (hasChanges) {
            savedRound = innovationRoundRepository.save(round);
        } else {
            savedRound = round; // No changes, return existing round
        }

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
        // Thêm sort mặc định theo ngày tạo mới nhất trước nếu chưa có sort
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

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
        // Thêm sort mặc định theo ngày tạo mới nhất trước nếu chưa có sort
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

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

    // Helper method to check if phases have changed
    private boolean checkPhasesChanged(InnovationRound round, List<InnovationPhaseRequest> phaseRequests) {
        Set<InnovationPhase> existingPhases = round.getInnovationPhases();

        // If no existing phases and new phases provided, there are changes
        if (existingPhases == null || existingPhases.isEmpty()) {
            return !phaseRequests.isEmpty();
        }

        // If no new phases provided but existing phases exist, there are changes
        // (removal)
        if (phaseRequests.isEmpty()) {
            return true;
        }

        // Create a map of existing phases by phaseType for quick lookup
        Map<InnovationPhaseTypeEnum, InnovationPhase> existingPhaseMap = existingPhases.stream()
                .collect(Collectors.toMap(InnovationPhase::getPhaseType, phase -> phase));

        // Check if number of phases changed
        if (existingPhaseMap.size() != phaseRequests.size()) {
            return true;
        }

        // Check each phase request against existing phases
        for (InnovationPhaseRequest phaseRequest : phaseRequests) {
            InnovationPhaseTypeEnum phaseType = phaseRequest.getPhaseType();

            if (!existingPhaseMap.containsKey(phaseType)) {
                return true; // New phase type added
            }

            InnovationPhase existingPhase = existingPhaseMap.get(phaseType);

            // Check if any field has changed
            if (phaseRequest.getName() != null && !phaseRequest.getName().equals(existingPhase.getName())) {
                return true;
            }
            if (phaseRequest.getPhaseStartDate() != null
                    && !phaseRequest.getPhaseStartDate().equals(existingPhase.getPhaseStartDate())) {
                return true;
            }
            if (phaseRequest.getPhaseEndDate() != null
                    && !phaseRequest.getPhaseEndDate().equals(existingPhase.getPhaseEndDate())) {
                return true;
            }
            if (phaseRequest.getDescription() != null
                    && !phaseRequest.getDescription().equals(existingPhase.getDescription())) {
                return true;
            }
        }

        return false; // No changes detected
    }

    // Helper method to update phases for a round
    private void updatePhasesForRound(InnovationRound round, List<InnovationPhaseRequest> phaseRequests) {
        // Get existing phases
        Set<InnovationPhase> existingPhases = round.getInnovationPhases();

        // Create a map of existing phases by phaseType for quick lookup
        Map<InnovationPhaseTypeEnum, InnovationPhase> existingPhaseMap = existingPhases.stream()
                .collect(Collectors.toMap(InnovationPhase::getPhaseType, phase -> phase));

        // Process each phase request
        for (InnovationPhaseRequest phaseRequest : phaseRequests) {
            InnovationPhaseTypeEnum phaseType = phaseRequest.getPhaseType();

            if (existingPhaseMap.containsKey(phaseType)) {
                // Update existing phase - chỉ cập nhật khi có thay đổi
                InnovationPhase existingPhase = existingPhaseMap.get(phaseType);
                boolean phaseHasChanges = false;

                if (phaseRequest.getName() != null && !phaseRequest.getName().equals(existingPhase.getName())) {
                    existingPhase.setName(phaseRequest.getName());
                    phaseHasChanges = true;
                }
                if (phaseRequest.getPhaseStartDate() != null
                        && !phaseRequest.getPhaseStartDate().equals(existingPhase.getPhaseStartDate())) {
                    existingPhase.setPhaseStartDate(phaseRequest.getPhaseStartDate());
                    phaseHasChanges = true;
                }
                if (phaseRequest.getPhaseEndDate() != null
                        && !phaseRequest.getPhaseEndDate().equals(existingPhase.getPhaseEndDate())) {
                    existingPhase.setPhaseEndDate(phaseRequest.getPhaseEndDate());
                    phaseHasChanges = true;
                }
                if (phaseRequest.getDescription() != null
                        && !phaseRequest.getDescription().equals(existingPhase.getDescription())) {
                    existingPhase.setDescription(phaseRequest.getDescription());
                    phaseHasChanges = true;
                }

                // Save updated phase only if there are changes
                if (phaseHasChanges) {
                    innovationPhaseRepository.save(existingPhase);
                }
            } else {
                // Create new phase
                InnovationPhase newPhase = createPhaseFromRequest(round, phaseRequest);
                round.getInnovationPhases().add(newPhase);
            }
        }
    }

    // Helper method to create phase from request
    private InnovationPhase createPhaseFromRequest(InnovationRound round, InnovationPhaseRequest request) {
        InnovationPhase phase = new InnovationPhase();
        phase.setInnovationRound(round);
        phase.setName(request.getName());
        phase.setPhaseType(request.getPhaseType());
        phase.setPhaseStartDate(request.getPhaseStartDate());
        phase.setPhaseEndDate(request.getPhaseEndDate());
        phase.setDescription(request.getDescription());
        phase.setPhaseStatus(PhaseStatusEnum.PENDING);
        phase.setIsDeadline(false);

        return innovationPhaseRepository.save(phase);
    }

}
