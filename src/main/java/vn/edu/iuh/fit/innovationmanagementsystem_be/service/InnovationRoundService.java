package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationRoundRequest;
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

    // 1. Tạo innovationRound
    @Transactional
    public InnovationRoundResponse createInnovationRound(CreateInnovationRoundRequest request) {

        // Tạo innovationRound
        InnovationRound round = new InnovationRound();
        round.setName(request.getName());
        round.setAcademicYear(request.getAcademicYear());
        round.setDescription(request.getDescription());
        round.setRegistrationStartDate(request.getRegistrationStartDate());
        round.setRegistrationEndDate(request.getRegistrationEndDate());
        round.setStatus(request.getStatus());

        // Tạo InnovationDecision
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

        // Lưu innovationRound - lấy ID
        round = innovationRoundRepository.save(round);

        // Tạo InnovationPhase
        if (request.getInnovationPhase() != null && !request.getInnovationPhase().isEmpty()) {
            Set<InnovationPhase> phases = innovationPhaseService.createPhasesForRound(round,
                    request.getInnovationPhase());

            round.getInnovationPhases().clear();
            round.getInnovationPhases().addAll(phases);
        }

        return innovationRoundMapper.toInnovationRoundResponse(innovationRoundRepository.save(round));
    }

    // 5. Cập nhật innovationRound
    public InnovationRoundResponse updateRound(String roundId, UpdateInnovationRoundRequest request) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        boolean hasChanges = false;

        // Cập nhập thông tin basic innovationRound
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

        // Cập nhập InnovationDecision
        if (request.getInnovationDecision() != null) {
            if (round.getInnovationDecision() != null) {
                // Cập nhập existing decision
                InnovationDecision existingDecision = round.getInnovationDecision();
                boolean decisionHasChanges = false;

                // Cập nhập fields decision
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
                // Tạo new decision nếu innovationRound không có
                InnovationDecision decisionReq = innovationDecisionService
                        .createDecision(request.getInnovationDecision());
                round.setInnovationDecision(decisionReq);
                hasChanges = true;
            }
        }

        // Cập nhập InnovationPhase
        if (request.getInnovationPhase() != null && !request.getInnovationPhase().isEmpty()) {

            boolean phasesChanged = checkPhasesChanged(round, new ArrayList<>(request.getInnovationPhase()));
            if (phasesChanged) {
                // Lưu innovationRound để lấy ID trước khi cập nhập phases
                if (hasChanges) {
                    round = innovationRoundRepository.save(round);
                }
                // Cập nhập existing phases hoặc tạo mới
                updatePhasesForRound(round, new ArrayList<>(request.getInnovationPhase()));
                hasChanges = true;
            }
        }

        // Chỉ lưu nếu có thay đổi thực sự
        InnovationRound savedRound;
        if (hasChanges) {
            savedRound = innovationRoundRepository.save(round);
        } else {
            savedRound = round; // Không có thay đổi, trả về existing rounds
        }

        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(savedRound);
        setStatistics(response, savedRound);
        return response;
    }

    // 9. Lấy tất cả innovationRound với pagination và filtering
    public ResultPaginationDTO getAllInnovationRoundsWithPaginationAndFilter(
            Specification<InnovationRound> specification, Pageable pageable) {

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

    // 10. Lấy innovationRound list với pagination và filtering
    public ResultPaginationDTO getInnovationRoundsListForTable(
            Specification<InnovationRound> specification, Pageable pageable) {

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

    /*
     * Helper method: Chuyển đổi InnovationRound sang InnovationRoundListResponse
     */
    private InnovationRoundListResponse convertToListResponse(InnovationRound round) {
        InnovationRoundListResponse response = new InnovationRoundListResponse();
        response.setId(round.getId());
        response.setName(round.getName());
        response.setAcademicYear(round.getAcademicYear());
        response.setRegistrationStartDate(round.getRegistrationStartDate());
        response.setRegistrationEndDate(round.getRegistrationEndDate());
        response.setStatus(round.getStatus());

        // Đếm innovation phases
        response.setPhaseCount(round.getInnovationPhases() != null ? round.getInnovationPhases().size() : 0);

        // Đếm scoring criteria từ InnovationDecision
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

    /*
     * Helper method: Đặt thống kê cho InnovationRoundResponse
     */
    private void setStatistics(InnovationRoundResponse response, InnovationRound round) {
        if (round.getInnovations() != null) {
            int submissionCount = (int) round.getInnovations().stream()
                    .filter(innovation -> innovation.getStatus() == InnovationStatusEnum.SUBMITTED)
                    .count();
            response.setSubmissionCount(submissionCount);

            int reviewedCount = (int) round.getInnovations().stream()
                    .filter(innovation -> innovation.getStatus() == InnovationStatusEnum.PENDING_TRUONG_REVIEW)
                    .count();
            response.setReviewedCount(reviewedCount);

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

    /*
     * Helper method: Kiểm tra nếu phases có thay đổi
     */
    private boolean checkPhasesChanged(InnovationRound round, List<InnovationPhaseRequest> phaseRequests) {
        Set<InnovationPhase> existingPhases = round.getInnovationPhases();

        // Nếu không có phases tồn tại và new phases được cung cấp, có thay đổi
        if (existingPhases == null || existingPhases.isEmpty()) {
            return !phaseRequests.isEmpty();
        }

        // Nếu không có new phases được cung cấp nhưng phases tồn tại, có thay đổi
        if (phaseRequests.isEmpty()) {
            return true;
        }

        // Tạo a map of existing phases by phaseType for quick lookup
        Map<InnovationPhaseTypeEnum, InnovationPhase> existingPhaseMap = existingPhases.stream()
                .collect(Collectors.toMap(InnovationPhase::getPhaseType, phase -> phase));

        // Kiểm tra nếu số lượng phases thay đổi
        if (existingPhaseMap.size() != phaseRequests.size()) {
            return true;
        }

        // Kiểm tra mỗi phase request từ existing phases
        for (InnovationPhaseRequest phaseRequest : phaseRequests) {
            InnovationPhaseTypeEnum phaseType = phaseRequest.getPhaseType();

            if (!existingPhaseMap.containsKey(phaseType)) {
                return true; // New phase type được thêm
            }

            InnovationPhase existingPhase = existingPhaseMap.get(phaseType);

            // Kiểm tra nếu bất kỳ field nào có thay đổi
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

        return false;
    }

    /*
     * Helper method: Cập nhập phases cho một round
     */
    private void updatePhasesForRound(InnovationRound round, List<InnovationPhaseRequest> phaseRequests) {
        // Lấy existing phases
        Set<InnovationPhase> existingPhases = round.getInnovationPhases();

        // Tạo a map of existing phases by phaseType for quick lookup
        Map<InnovationPhaseTypeEnum, InnovationPhase> existingPhaseMap = existingPhases.stream()
                .collect(Collectors.toMap(InnovationPhase::getPhaseType, phase -> phase));

        // Xử lý mỗi phase request
        for (InnovationPhaseRequest phaseRequest : phaseRequests) {
            InnovationPhaseTypeEnum phaseType = phaseRequest.getPhaseType();

            if (existingPhaseMap.containsKey(phaseType)) {
                // Cập nhập existing phase - chỉ cập nhật khi có thay đổi
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

                if (phaseHasChanges) {
                    innovationPhaseRepository.save(existingPhase);
                }
            } else {
                InnovationPhase newPhase = createPhaseFromRequest(round, phaseRequest);
                round.getInnovationPhases().add(newPhase);
            }
        }
    }

    /*
     * Helper method: Tạo phase từ request
     */
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
