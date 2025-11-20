package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationPhaseRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.event.InnovationRoundPublishedEvent;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundListResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationRoundMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    public InnovationRoundService(InnovationDecisionService innovationDecisionService,
            InnovationRoundRepository innovationRoundRepository,
            InnovationDecisionRepository innovationDecisionRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            InnovationRoundMapper innovationRoundMapper, InnovationPhaseService innovationPhaseService,
            org.springframework.context.ApplicationEventPublisher eventPublisher, UserService userService) {
        this.innovationDecisionService = innovationDecisionService;
        this.innovationRoundRepository = innovationRoundRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.innovationRoundMapper = innovationRoundMapper;
        this.innovationPhaseService = innovationPhaseService;
        this.eventPublisher = eventPublisher;
        this.userService = userService;
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

        round.setStatus(InnovationRoundStatusEnum.DRAFT);

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
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("createdAt").descending());
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
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("createdAt").descending());
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
        // Kiểm tra xem có round nào với status OPEN không
        Optional<InnovationRound> currentRound = innovationRoundRepository.findCurrentActiveRound(
                LocalDate.now(), InnovationRoundStatusEnum.OPEN);

        if (currentRound.isPresent()) {
            InnovationRound round = currentRound.get();
            InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
            setStatistics(response, round);
            return response;
        }

        // Nếu không có round OPEN, kiểm tra xem có round nào không
        Optional<InnovationRound> latestRound = innovationRoundRepository.findLatestRound();

        if (latestRound.isEmpty()) {
            throw new IdInvalidException("Hiện tại không có đợt sáng kiến nào trong hệ thống");
        }

        InnovationRound round = latestRound.get();
        if (InnovationRoundStatusEnum.DRAFT.equals(round.getStatus())) {
            throw new IdInvalidException(
                    "Đợt sáng kiến '" + round.getName() + "' đang ở trạng thái DRAFT. " +
                            "Vui lòng công bố đợt sáng kiến để có thể sử dụng");
        }

        throw new IdInvalidException(
                "Hiện tại không có đợt sáng kiến nào đang mở. " +
                        "Đợt sáng kiến gần nhất '" + round.getName() + "' có trạng thái: "
                        + round.getStatus().getValue());
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
            if (phaseRequest.getIsDeadline() != null
                    && !phaseRequest.getIsDeadline().equals(existingPhase.getIsDeadline())) {
                return true;
            }
            if (phaseRequest.getAllowLateSubmission() != null
                    && !phaseRequest.getAllowLateSubmission().equals(existingPhase.getAllowLateSubmission())) {
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
                if (phaseRequest.getAllowLateSubmission() != null
                        && !phaseRequest.getAllowLateSubmission().equals(existingPhase.getAllowLateSubmission())) {
                    existingPhase.setAllowLateSubmission(phaseRequest.getAllowLateSubmission());
                    phaseHasChanges = true;
                }
                if (phaseRequest.getIsDeadline() != null
                        && !phaseRequest.getIsDeadline().equals(existingPhase.getIsDeadline())) {
                    existingPhase.setIsDeadline(phaseRequest.getIsDeadline());
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
        phase.setPhaseStatus(PhaseStatusEnum.SCHEDULED);
        if (request.getIsDeadline() != null) {
            phase.setIsDeadline(request.getIsDeadline());
        } else {
            phase.setIsDeadline(false);
        }
        if (request.getAllowLateSubmission() != null) {
            phase.setAllowLateSubmission(request.getAllowLateSubmission());
        }

        return innovationPhaseRepository.save(phase);
    }

    // 11. Công bố Round - chuyển từ DRAFT sang OPEN
    @Transactional
    public InnovationRoundResponse publishRound(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        // Kiểm tra điều kiện để công bố Round
        validatePublishRoundConditions(round);

        // Chuyển status từ DRAFT sang OPEN
        round.setStatus(InnovationRoundStatusEnum.OPEN);

        InnovationRound savedRound = innovationRoundRepository.save(round);
        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(savedRound);
        setStatistics(response, savedRound);

        User actor = userService.getCurrentUser();

        // Publish event - Event listener sẽ xử lý notification
        eventPublisher
                .publishEvent(new InnovationRoundPublishedEvent(
                        this, savedRound.getId(), savedRound.getName(), actor.getId(), actor.getFullName()));

        return response;
    }

    // 12. Đóng Round - chuyển sang CLOSED
    @Transactional
    public InnovationRoundResponse closeRound(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        // Kiểm tra điều kiện để đóng Round
        validateCloseRoundConditions(round);

        // Chuyển status sang CLOSED
        round.setStatus(InnovationRoundStatusEnum.CLOSED);

        InnovationRound savedRound = innovationRoundRepository.save(round);
        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(savedRound);
        setStatistics(response, savedRound);

        User actor = userService.getCurrentUser();

        // Publish event - Event listener sẽ xử lý notification
        eventPublisher.publishEvent(new vn.edu.iuh.fit.innovationmanagementsystem_be.event.InnovationRoundClosedEvent(
                this, savedRound.getId(), savedRound.getName(), actor.getId(), actor.getFullName()));

        return response;
    }

    /*
     * Helper method: Kiểm tra điều kiện để công bố Round
     */
    private void validatePublishRoundConditions(InnovationRound round) {
        // 1. Kiểm tra status hiện tại phải là DRAFT
        if (round.getStatus() != InnovationRoundStatusEnum.DRAFT) {
            throw new IllegalArgumentException(
                    "Chỉ có thể công bố Round có trạng thái DRAFT. Trạng thái hiện tại: " + round.getStatus());
        }

        // 2. Kiểm tra có InnovationDecision không
        if (round.getInnovationDecision() == null) {
            throw new IllegalArgumentException("Không thể công bố Round khi chưa có quyết định sáng kiến");
        }

        // 3. Kiểm tra có ít nhất 3 InnovationPhase
        if (round.getInnovationPhases() == null || round.getInnovationPhases().size() < 3) {
            throw new IllegalArgumentException("Round phải có ít nhất 3 giai đoạn sáng kiến để có thể công bố");
        }

        // 4. Kiểm tra ngày đăng ký hợp lệ chi tiết
        validateRegistrationDates(round);

        // 5. Kiểm tra ngày của các phase hợp lệ
        validatePhaseDates(round);

        // 6. Kiểm tra InnovationDecision có đầy đủ thông tin không
        validateInnovationDecision(round.getInnovationDecision());

        // 7. Kiểm tra FormTemplate có ít nhất 5 mẫu
        validateFormTemplates(round);

        // 8. Kiểm tra năm học hợp lệ
        validateAcademicYear(round);

        // 9. Kiểm tra tên Round không trùng lặp trong cùng năm học
        validateRoundNameUnique(round);

        // 9. Kiểm tra Phase phải có đầy đủ 3 loại: SUBMISSION, SCORING, ANNOUNCEMENT
        validatePhaseTypes(round);

        // 10. Kiểm tra FormTemplate phải có đầy đủ các loại template cần thiết
        validateTemplateTypes(round);

        // 11. Kiểm tra thời gian phase phải hợp lý
        validatePhaseDuration(round);

        // 12. Kiểm tra Round không được công bố nếu đã có Round khác đang ACTIVE
        validateActiveRound(round);
    }

    /*
     * Helper method: Kiểm tra ngày đăng ký hợp lệ
     */
    private void validateRegistrationDates(InnovationRound round) {
        if (round.getRegistrationStartDate() == null || round.getRegistrationEndDate() == null) {
            throw new IllegalArgumentException("Không thể công bố Round khi chưa có ngày đăng ký");
        }

        // Kiểm tra ngày đăng ký kết thúc phải sau ngày bắt đầu
        if (round.getRegistrationEndDate().isBefore(round.getRegistrationStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc đăng ký phải sau ngày bắt đầu đăng ký");
        }

    }

    /*
     * Helper method: Kiểm tra ngày của các phase hợp lệ
     */
    private void validatePhaseDates(InnovationRound round) {
        Set<InnovationPhase> phasesSet = round.getInnovationPhases();
        List<InnovationPhase> phases = new ArrayList<>(phasesSet);

        // Kiểm tra từng phase có đầy đủ ngày tháng
        for (InnovationPhase phase : phases) {
            if (phase.getPhaseStartDate() == null || phase.getPhaseEndDate() == null) {
                throw new IllegalArgumentException("Tất cả các giai đoạn phải có đầy đủ ngày bắt đầu và kết thúc");
            }

            // Kiểm tra ngày kết thúc phải sau ngày bắt đầu
            if (phase.getPhaseEndDate().isBefore(phase.getPhaseStartDate())) {
                throw new IllegalArgumentException(
                        "Ngày kết thúc của giai đoạn '" + phase.getName() + "' phải sau ngày bắt đầu"
                                + phase.getPhaseStartDate());
            }
        }

        // Sắp xếp phases theo thứ tự thời gian để kiểm tra sequence đúng
        List<InnovationPhase> sortedPhases = phases.stream()
                .sorted(Comparator.comparing(InnovationPhase::getPhaseStartDate))
                .collect(Collectors.toList());

        // Kiểm tra phase sau phải bắt đầu sau khi phase trước kết thúc
        for (int i = 0; i < sortedPhases.size() - 1; i++) {
            InnovationPhase currentPhase = sortedPhases.get(i);
            InnovationPhase nextPhase = sortedPhases.get(i + 1);

            if (nextPhase.getPhaseStartDate().isBefore(currentPhase.getPhaseEndDate())) {
                throw new IllegalArgumentException("Giai đoạn '" + nextPhase.getName() +
                        "' phải bắt đầu sau khi giai đoạn '" + currentPhase.getName() + "' kết thúc");
            }
        }

    }

    /*
     * Helper method: Kiểm tra InnovationDecision có đầy đủ thông tin
     */
    private void validateInnovationDecision(InnovationDecision decision) {
        if (decision.getDecisionNumber() == null || decision.getDecisionNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Quyết định sáng kiến phải có số quyết định");
        }
        if (decision.getTitle() == null || decision.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Quyết định sáng kiến phải có tiêu đề");
        }
        if (decision.getPromulgatedDate() == null) {
            throw new IllegalArgumentException("Quyết định sáng kiến phải có ngày ban hành");
        }

        // Kiểm tra ngày ban hành không được trong tương lai
        if (decision.getPromulgatedDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày ban hành quyết định không được trong tương lai");
        }
    }

    /*
     * Helper method: Kiểm tra FormTemplate có ít nhất 5 mẫu
     */
    private void validateFormTemplates(InnovationRound round) {
        if (round.getFormTemplates() == null || round.getFormTemplates().size() < 5) {
            throw new IllegalArgumentException("Round phải có ít nhất 5 mẫu form để có thể công bố");
        }

        // Kiểm tra từng form template có đầy đủ thông tin cơ bản
        for (vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate template : round
                .getFormTemplates()) {
            if (template.getTemplateType() == null) {
                throw new IllegalArgumentException("Tất cả mẫu FormTemplate phải có loại TemplateType");
            }
            if (template.getTargetRole() == null) {
                throw new IllegalArgumentException("Tất cả mẫu FormTemplate phải có vai TargetRole");
            }
            if (template.getTemplateContent() == null || template.getTemplateContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Tất cả mẫu FormTemplate phải có nội dung TemplateContent");
            }
            if (template.getFormFields() == null || template.getFormFields().isEmpty()) {
                throw new IllegalArgumentException("Tất cả mẫu FormTemplate phải có ít nhất một trường FormField");
            }
        }
    }

    /*
     * Helper method: Kiểm tra năm học hợp lệ
     */
    private void validateAcademicYear(InnovationRound round) {
        if (round.getAcademicYear() == null || round.getAcademicYear().trim().isEmpty()) {
            throw new IllegalArgumentException("Năm học không được để trống");
        }

        // Kiểm tra format năm học (ví dụ: 2023-2024, 2024-2025)
        String academicYear = round.getAcademicYear().trim();
        if (!academicYear.matches("^\\d{4}-\\d{4}$")) {
            throw new IllegalArgumentException("Năm học phải có định dạng YYYY-YYYY (ví dụ: 2023-2024)");
        }

        // Kiểm tra năm học hợp lệ (năm sau phải bằng năm trước + 1)
        String[] years = academicYear.split("-");
        try {
            int startYear = Integer.parseInt(years[0]);
            int endYear = Integer.parseInt(years[1]);

            if (endYear != startYear + 1) {
                throw new IllegalArgumentException("Năm học không hợp lệ: năm kết thúc phải bằng năm bắt đầu + 1");
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Năm học phải là số nguyên hợp lệ");
        }
    }

    /*
     * Helper method: Kiểm tra tên Round không trùng lặp trong cùng năm học
     */
    private void validateRoundNameUnique(InnovationRound round) {
        if (round.getName() == null || round.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đợt không được để trống");
        }

        // Kiểm tra trùng lặp tên trong cùng năm học
        List<InnovationRound> existingRounds = innovationRoundRepository.findByAcademicYearAndNameIgnoreCase(
                round.getAcademicYear(), round.getName().trim());

        // Loại bỏ chính nó khỏi kết quả nếu đang update
        existingRounds = existingRounds.stream()
                .filter(r -> !r.getId().equals(round.getId()))
                .collect(Collectors.toList());

        if (!existingRounds.isEmpty()) {
            throw new IllegalArgumentException("Tên đợt '" + round.getName() +
                    "' đã tồn tại trong năm học " + round.getAcademicYear());
        }
    }

    /*
     * Helper method: Kiểm tra Phase phải có đầy đủ 3 loại: SUBMISSION, SCORING,
     * ANNOUNCEMENT
     */
    private void validatePhaseTypes(InnovationRound round) {
        Set<InnovationPhase> phases = round.getInnovationPhases();
        Set<InnovationPhaseTypeEnum> phaseTypes = phases.stream()
                .map(InnovationPhase::getPhaseType)
                .collect(Collectors.toSet());

        // Kiểm tra phải có đầy đủ 3 loại phase
        if (!phaseTypes.contains(InnovationPhaseTypeEnum.SUBMISSION)) {
            throw new IllegalArgumentException("Round phải có ít nhất một giai đoạn SUBMISSION");
        }
        if (!phaseTypes.contains(InnovationPhaseTypeEnum.SCORING)) {
            throw new IllegalArgumentException("Round phải có ít nhất một giai đoạn SCORING");
        }
        if (!phaseTypes.contains(InnovationPhaseTypeEnum.ANNOUNCEMENT)) {
            throw new IllegalArgumentException("Round phải có ít nhất một giai đoạn ANNOUNCEMENT");
        }

        // Kiểm tra thứ tự phase hợp lý (SUBMISSION -> SCORING -> ANNOUNCEMENT)
        List<InnovationPhase> sortedPhases = phases.stream()
                .sorted(Comparator.comparing(InnovationPhase::getPhaseStartDate))
                .collect(Collectors.toList());

        // Kiểm tra thứ tự phase type theo thời gian
        InnovationPhaseTypeEnum[] expectedOrder = {
                InnovationPhaseTypeEnum.SUBMISSION,
                InnovationPhaseTypeEnum.SCORING,
                InnovationPhaseTypeEnum.ANNOUNCEMENT
        };

        int expectedIndex = 0;
        for (InnovationPhase phase : sortedPhases) {
            InnovationPhaseTypeEnum currentType = phase.getPhaseType();

            // Tìm phase type hiện tại trong expected order
            while (expectedIndex < expectedOrder.length &&
                    !expectedOrder[expectedIndex].equals(currentType)) {
                expectedIndex++;
            }

            // Nếu không tìm thấy hoặc đã vượt quá expected order
            if (expectedIndex >= expectedOrder.length) {
                throw new IllegalArgumentException(
                        "Thứ tự giai đoạn không hợp lệ. Giai đoạn '" + phase.getName() +
                                "' (" + currentType + ") không đúng thứ tự. " +
                                "Thứ tự đúng phải là: SUBMISSION -> SCORING -> ANNOUNCEMENT");
            }
        }

        // Kiểm tra có đủ 3 loại phase không
        if (expectedIndex < expectedOrder.length - 1) {
            throw new IllegalArgumentException(
                    "Thiếu giai đoạn '" + expectedOrder[expectedIndex + 1] +
                            "'. Phải có đầy đủ 3 giai đoạn: SUBMISSION -> SCORING -> ANNOUNCEMENT");
        }
    }

    /*
     * Helper method: Kiểm tra FormTemplate phải có đầy đủ các loại template cần
     * thiết
     */
    private void validateTemplateTypes(InnovationRound round) {
        List<FormTemplate> templates = round.getFormTemplates();
        Set<TemplateTypeEnum> templateTypes = templates.stream()
                .map(FormTemplate::getTemplateType)
                .collect(Collectors.toSet());

        // Kiểm tra các loại template bắt buộc
        Set<TemplateTypeEnum> requiredTypes = Set.of(
                TemplateTypeEnum.DON_DE_NGHI,
                TemplateTypeEnum.BAO_CAO_MO_TA,
                TemplateTypeEnum.BIEN_BAN_HOP,
                TemplateTypeEnum.TONG_HOP_DE_NGHI,
                TemplateTypeEnum.TONG_HOP_CHAM_DIEM);

        for (TemplateTypeEnum requiredType : requiredTypes) {
            if (!templateTypes.contains(requiredType)) {
                throw new IllegalArgumentException("InnovationRound phải có TemplateType: " + requiredType.getValue());
            }
        }

        // Kiểm tra không có template trùng lặp loại
        Map<TemplateTypeEnum, Long> typeCounts = templates.stream()
                .collect(Collectors.groupingBy(FormTemplate::getTemplateType, Collectors.counting()));

        for (Map.Entry<TemplateTypeEnum, Long> entry : typeCounts.entrySet()) {
            if (entry.getValue() > 1) {
                throw new IllegalArgumentException("Không được có nhiều hơn 1 template cùng loại: " +
                        entry.getKey().getValue());
            }
        }
    }

    /*
     * Helper method: Kiểm tra thời gian phase phải hợp lý
     */
    private void validatePhaseDuration(InnovationRound round) {
        Set<InnovationPhase> phases = round.getInnovationPhases();

        // Sắp xếp phases theo thứ tự để kiểm tra sequence
        List<InnovationPhase> sortedPhases = phases.stream()
                .sorted(Comparator.comparing(InnovationPhase::getPhaseStartDate))
                .collect(Collectors.toList());

        // 1. Kiểm tra từng phase riêng lẻ
        for (InnovationPhase phase : sortedPhases) {
            if (phase.getPhaseEndDate().isBefore(phase.getPhaseStartDate())) {
                throw new IllegalArgumentException("Giai đoạn '" + phase.getName() +
                        "' có ngày kết thúc (" + phase.getPhaseEndDate() +
                        ") phải sau ngày bắt đầu (" + phase.getPhaseStartDate() + ")");
            }

            long daysBetween = ChronoUnit.DAYS.between(phase.getPhaseStartDate(), phase.getPhaseEndDate());

            // Kiểm tra phase không được quá ngắn (ít nhất 1 ngày)
            if (daysBetween < 1) {
                throw new IllegalArgumentException("Giai đoạn '" + phase.getName() +
                        "' phải có thời gian ít nhất 1 ngày");
            }

        }

        // 2. Kiểm tra sequence của các phases
        for (int i = 0; i < sortedPhases.size() - 1; i++) {
            InnovationPhase currentPhase = sortedPhases.get(i);
            InnovationPhase nextPhase = sortedPhases.get(i + 1);

            // Kiểm tra phase sau phải bắt đầu sau ngày kết thúc của phase trước
            if (!nextPhase.getPhaseStartDate().isAfter(currentPhase.getPhaseEndDate())) {
                throw new IllegalArgumentException(
                        "Giai đoạn '" + nextPhase.getName() + "' phải bắt đầu sau ngày kết thúc của giai đoạn '" +
                                currentPhase.getName() + "' (" + currentPhase.getPhaseEndDate() +
                                "). Ngày bắt đầu hiện tại: " + nextPhase.getPhaseStartDate());
            }

        }
    }

    /*
     * Helper method: Kiểm tra Round không được công bố nếu đã có Round khác đang
     * OPEN
     */
    private void validateActiveRound(InnovationRound round) {
        List<InnovationRound> activeRounds = innovationRoundRepository.findByStatus(InnovationRoundStatusEnum.OPEN);

        // Loại bỏ chính nó khỏi kết quả nếu đang update
        activeRounds = activeRounds.stream()
                .filter(r -> !r.getId().equals(round.getId()))
                .collect(Collectors.toList());

        if (!activeRounds.isEmpty()) {
            InnovationRound activeRound = activeRounds.get(0);
            throw new IllegalArgumentException("Không thể công bố Round mới khi đã có Round '" +
                    activeRound.getName() + "' đang ở trạng thái OPEN");
        }
    }

    /*
     * Helper method: Kiểm tra điều kiện để đóng Round
     */
    private void validateCloseRoundConditions(InnovationRound round) {
        // 1. Kiểm tra status hiện tại phải là OPEN
        if (round.getStatus() != InnovationRoundStatusEnum.OPEN) {
            throw new IllegalArgumentException(
                    "Chỉ có thể đóng Round có trạng thái OPEN. Trạng thái hiện tại: " + round.getStatus());
        }

        // 2. Kiểm tra đã qua ngày kết thúc đăng ký chưa
        LocalDate today = LocalDate.now();
        if (today.isBefore(round.getRegistrationEndDate())) {
            throw new IllegalArgumentException("Chưa đến ngày kết thúc đăng ký, không thể đóng Round");
        }

        // 3. Kiểm tra tất cả Phase đã hoàn thành
        validateAllPhasesCompleted(round);

    }

    /*
     * Helper method: Kiểm tra tất cả Phase đã hoàn thành
     */
    private void validateAllPhasesCompleted(InnovationRound round) {
        LocalDate today = LocalDate.now();
        Set<InnovationPhase> phases = round.getInnovationPhases();

        for (InnovationPhase phase : phases) {
            // Kiểm tra phase ANNOUNCEMENT phải đã kết thúc
            if (phase.getPhaseType() == InnovationPhaseTypeEnum.ANNOUNCEMENT) {
                if (today.isBefore(phase.getPhaseEndDate())) {
                    throw new IllegalArgumentException("Không thể đóng Round khi giai đoạn ANNOUNCEMENT chưa kết thúc");
                }
            }
        }
    }

    // 13. Get Round by ID
    public InnovationRoundResponse getRoundById(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));
        InnovationRoundResponse response = innovationRoundMapper.toInnovationRoundResponse(round);
        setStatistics(response, round);
        return response;
    }

    // 14. Delete Round (Only DRAFT status)
    @Transactional
    public void deleteRound(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        // Kiểm tra status phải là DRAFT
        if (round.getStatus() != InnovationRoundStatusEnum.DRAFT) {
            throw new IllegalArgumentException(
                    "Chỉ có thể xóa Round có trạng thái DRAFT. Trạng thái hiện tại: " + round.getStatus().getValue());
        }

        // Xóa round (cascade sẽ tự động xóa các entities liên quan)
        innovationRoundRepository.delete(round);
    }
}
