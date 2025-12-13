package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ScoreCriteriaDetail;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.SubmitInnovationScoreRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationScoreResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.ReviewScoreMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReviewLevelEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ViolationTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReviewScoreRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewScoreService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewScoreService.class);

    private final ReviewScoreRepository reviewScoreRepository;
    private final InnovationRepository innovationRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final DepartmentPhaseRepository departmentPhaseRepository;
    private final InnovationPhaseRepository innovationPhaseRepository;
    private final UserService userService;
    private final ReviewScoreMapper reviewScoreMapper;
    private final ObjectMapper objectMapper;

    public ReviewScoreService(
            ReviewScoreRepository reviewScoreRepository,
            InnovationRepository innovationRepository,
            CouncilMemberRepository councilMemberRepository,
            DepartmentPhaseRepository departmentPhaseRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            UserService userService,
            ReviewScoreMapper reviewScoreMapper,
            ObjectMapper objectMapper) {
        this.reviewScoreRepository = reviewScoreRepository;
        this.innovationRepository = innovationRepository;
        this.councilMemberRepository = councilMemberRepository;
        this.departmentPhaseRepository = departmentPhaseRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.userService = userService;
        this.reviewScoreMapper = reviewScoreMapper;
        this.objectMapper = objectMapper;
    }

    // 1. Chấm điểm sáng kiến
    public InnovationScoreResponse submitInnovationScore(String innovationId, SubmitInnovationScoreRequest request) {
        logger.info("Submitting score for innovation: {}", innovationId);

        // 1. Validate innovation exists
        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        // 2. Validate innovation có InnovationRound và InnovationDecision
        if (innovation.getInnovationRound() == null) {
            throw new IdInvalidException("Sáng kiến chưa được gán vào đợt sáng kiến nào");
        }

        if (innovation.getInnovationRound().getInnovationDecision() == null) {
            throw new IdInvalidException("Đợt sáng kiến chưa có quyết định đánh giá");
        }

        // 3. Get current user (reviewer)
        User reviewer = userService.getCurrentUser();

        // 4. Validate user là thành viên Hội đồng của innovation này
        validateUserIsCouncilMember(innovation, reviewer);

        // 5. Validate thời gian chấm điểm (chỉ cho phép chấm/cập nhật trong thời gian
        // chấm điểm)
        validateScoringPeriod(innovation);

        // 6. Check loại sáng kiến
        Boolean isScore = innovation.getIsScore();

        if (isScore != null && isScore) {
            // Sáng kiến CÓ chấm điểm
            // Validate scoring details và total score
            if (request.getScoringDetails() == null || request.getScoringDetails().isEmpty()) {
                throw new IdInvalidException("Danh sách điểm không được để trống cho sáng kiến có chấm điểm");
            }
            if (request.getTotalScore() == null) {
                throw new IdInvalidException("Tổng điểm không được để trống cho sáng kiến có chấm điểm");
            }

            validateScoringDetails(innovation, request.getScoringDetails());
            validateTotalScore(innovation, request.getScoringDetails(), request.getTotalScore());
        } else {
            // Sáng kiến KHÔNG chấm điểm
            // Không cần scoring details và total score
            if (request.getScoringDetails() != null && !request.getScoringDetails().isEmpty()) {
                throw new IdInvalidException(
                        "Sáng kiến này không cần chấm điểm. Vui lòng chỉ đánh giá thông qua/không thông qua");
            }
        }

        // 7. Check if user đã chấm điểm chưa
        Optional<ReviewScore> existingScore = reviewScoreRepository
                .findByInnovationIdAndReviewerId(innovationId, reviewer.getId());

        ReviewScore reviewScore;
        if (existingScore.isPresent()) {
            // Update existing score
            logger.info("Updating existing score for innovation: {} by reviewer: {}", innovationId, reviewer.getId());
            reviewScore = existingScore.get();
        } else {
            // Create new score
            logger.info("Creating new score for innovation: {} by reviewer: {}", innovationId, reviewer.getId());
            reviewScore = new ReviewScore();
            reviewScore.setInnovation(innovation);
            reviewScore.setReviewer(reviewer);
            reviewScore.setInnovationDecision(innovation.getInnovationRound().getInnovationDecision());
        }

        // 8. Xử lý vi phạm (nếu có)
        Boolean hasViolation = request.getHasViolation() != null && request.getHasViolation();
        if (hasViolation) {
            // Validate violation fields
            if (request.getViolationType() == null || request.getViolationType().trim().isEmpty()) {
                throw new IdInvalidException("Loại vi phạm không được để trống khi báo cáo vi phạm");
            }
            if (request.getViolationReason() == null || request.getViolationReason().trim().isEmpty()) {
                throw new IdInvalidException("Lý do vi phạm không được để trống khi báo cáo vi phạm");
            }

            // Validate violation type
            ViolationTypeEnum violationType;
            try {
                violationType = ViolationTypeEnum.valueOf(request.getViolationType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IdInvalidException("Loại vi phạm không hợp lệ: " + request.getViolationType() + 
                    ". Các loại hợp lệ: DUPLICATE, FEASIBILITY, POLICY_VIOLATION, OTHER");
            }

            // Set violation fields
            reviewScore.setHasViolation(true);
            reviewScore.setViolationType(violationType);
            reviewScore.setViolationReason(request.getViolationReason().trim());

            // Nếu báo vi phạm, tự động set isApproved = false (không thông qua)
            reviewScore.setIsApproved(false);
            logger.info("Violation reported for innovation {}: type={}, reason={}", 
                innovationId, violationType, request.getViolationReason());
        } else {
            // Không có vi phạm
            reviewScore.setHasViolation(false);
            reviewScore.setViolationType(null);
            reviewScore.setViolationReason(null);
        }

        // 9. Set scoring data dựa trên loại sáng kiến
        if (isScore != null && isScore) {
            // Sáng kiến CÓ chấm điểm
            reviewScore.setScoringDetails(objectMapper.valueToTree(request.getScoringDetails()));
            reviewScore.setTotalScore(request.getTotalScore());

            // Chỉ tự động thông qua nếu KHÔNG có vi phạm và >= 70% điểm tối đa
            if (!hasViolation) {
                InnovationDecision decision = innovation.getInnovationRound().getInnovationDecision();
                JsonNode scoringCriteria = decision != null ? decision.getScoringCriteria() : null;
                int maxTotalScore = calculateMaxTotalScore(scoringCriteria);
                double passingThreshold = maxTotalScore * 0.7; // 70%

                if (request.getTotalScore() >= passingThreshold) {
                    reviewScore.setIsApproved(true);
                    logger.info("Auto-approved innovation {} with score {} >= {} (70% of {})", 
                        innovationId, request.getTotalScore(), passingThreshold, maxTotalScore);
                } else {
                    // Null check để an toàn
                    if (request.getIsApproved() == null) {
                        throw new IdInvalidException("Quyết định đánh giá không được để trống");
                    }
                    reviewScore.setIsApproved(request.getIsApproved());
                }
            }
            // Nếu hasViolation = true, isApproved đã được set = false ở trên
        } else {
            // Sáng kiến KHÔNG chấm điểm
            reviewScore.setScoringDetails(null);
            reviewScore.setTotalScore(null);
            
            // Chỉ set isApproved nếu không có vi phạm (nếu có vi phạm thì đã set = false ở trên)
            if (!hasViolation) {
                // Null check để an toàn
                if (request.getIsApproved() == null) {
                    throw new IdInvalidException("Quyết định đánh giá không được để trống");
                }
                reviewScore.setIsApproved(request.getIsApproved());
            }
        }

        reviewScore.setRequiresSupplementaryDocuments(
                request.getRequiresSupplementaryDocuments() != null
                        ? request.getRequiresSupplementaryDocuments()
                        : false);
        reviewScore.setDetailedComments(request.getDetailedComments());
        reviewScore.setReviewedAt(LocalDateTime.now());

        // 9. Save
        ReviewScore savedScore = reviewScoreRepository.save(reviewScore);

        logger.info("Score submitted successfully for innovation: {}", innovationId);

        // 10. Return response
        return reviewScoreMapper.toInnovationScoreResponse(savedScore);
    }

    // 2. Lấy đánh giá đã chấm của user hiện tại cho một sáng kiến
    @Transactional(readOnly = true)
    public InnovationScoreResponse getMyEvaluation(String innovationId) {
        logger.info("Getting evaluation for innovation: {} by current user", innovationId);

        // 1. Validate innovation exists
        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        // 2. Get current user (reviewer)
        User reviewer = userService.getCurrentUser();

        // 3. Validate user là thành viên Hội đồng của innovation này
        validateUserIsCouncilMember(innovation, reviewer);

        // 4. Tìm đánh giá đã chấm
        Optional<ReviewScore> reviewScore = reviewScoreRepository
                .findByInnovationIdAndReviewerId(innovationId, reviewer.getId());

        if (reviewScore.isEmpty()) {
            throw new IdInvalidException("Bạn chưa chấm điểm cho sáng kiến này");
        }

        ReviewScore score = reviewScore.get();

        // 5. Map to response using mapper
        InnovationScoreResponse response = reviewScoreMapper.toInnovationScoreResponse(score);

        // 6. Manually map scoringDetails (workaround for MapStruct issue)
        if (score.getScoringDetails() != null && !score.getScoringDetails().isNull()
                && score.getScoringDetails().isArray()) {
            try {
                List<ScoreCriteriaDetail> scoringDetails = objectMapper.convertValue(
                        score.getScoringDetails(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ScoreCriteriaDetail.class));
                response.setScoringDetails(scoringDetails);
                logger.info("Manually mapped {} scoring details", scoringDetails.size());
            } catch (Exception e) {
                logger.error("Error manually mapping scoringDetails for reviewScore: {}", score.getId(), e);
                response.setScoringDetails(null);
            }
        }

        return response;
    }

    // Helper: Validate user là thành viên Hội đồng của innovation
    private void validateUserIsCouncilMember(Innovation innovation, User user) {
        // Lấy danh sách councils của innovation
        List<Council> councils = innovation.getCouncils();

        if (councils == null || councils.isEmpty()) {
            throw new IdInvalidException("Sáng kiến chưa được gán cho Hội đồng nào");
        }

        // Kiểm tra user có phải là thành viên của bất kỳ council nào không
        boolean isCouncilMember = councils.stream()
                .anyMatch(council -> {
                    List<CouncilMember> members = councilMemberRepository.findByCouncilId(council.getId());
                    return members.stream()
                            .anyMatch(member -> member.getUser().getId().equals(user.getId()));
                });

        if (!isCouncilMember) {
            throw new IdInvalidException(
                    "Bạn không phải là thành viên Hội đồng được gán cho sáng kiến này");
        }

        logger.info("User {} is a valid council member for innovation {}", user.getId(), innovation.getId());
    }

    // Helper: Tính maxTotalScore từ scoring criteria
    private int calculateMaxTotalScore(JsonNode scoringCriteria) {
        if (scoringCriteria == null || !scoringCriteria.isArray()) {
            return 100; // Default fallback
        }
        
        int maxTotal = 0;
        for (JsonNode criterion : scoringCriteria) {
            JsonNode subCriteria = criterion.get("subCriteria");
            if (subCriteria != null && subCriteria.isArray()) {
                int maxScoreForCriterion = 0;
                for (JsonNode subCriterion : subCriteria) {
                    if (subCriterion.has("maxScore")) {
                        maxScoreForCriterion = Math.max(maxScoreForCriterion, 
                            subCriterion.get("maxScore").asInt());
                    }
                }
                maxTotal += maxScoreForCriterion;
            }
        }
        return maxTotal > 0 ? maxTotal : 100; // Default fallback
    }

    // Helper: Validate scoring details match với scoring criteria
    private void validateScoringDetails(Innovation innovation, List<ScoreCriteriaDetail> scoringDetails) {
        InnovationDecision decision = innovation.getInnovationRound().getInnovationDecision();
        JsonNode scoringCriteria = decision.getScoringCriteria();

        if (scoringCriteria == null || !scoringCriteria.isArray()) {
            throw new IdInvalidException("Quyết định đánh giá không có bảng điểm");
        }

        // Convert JsonNode to Map for easier lookup
        Map<String, JsonNode> criteriaMap = new HashMap<>();
        for (JsonNode criterion : scoringCriteria) {
            String criteriaId = criterion.get("id").asText();
            criteriaMap.put(criteriaId, criterion);
        }

        // Validate each scoring detail
        Set<String> scoredCriteriaIds = new HashSet<>();
        for (ScoreCriteriaDetail detail : scoringDetails) {
            // Check if criteriaId exists
            if (!criteriaMap.containsKey(detail.getCriteriaId())) {
                throw new IdInvalidException(
                        "Tiêu chuẩn không hợp lệ: " + detail.getCriteriaId());
            }

            // Check duplicate
            if (scoredCriteriaIds.contains(detail.getCriteriaId())) {
                throw new IdInvalidException(
                        "Tiêu chuẩn bị trùng lặp: " + detail.getCriteriaId());
            }
            scoredCriteriaIds.add(detail.getCriteriaId());

            // Validate sub-criteria
            JsonNode criterion = criteriaMap.get(detail.getCriteriaId());
            JsonNode subCriteria = criterion.get("subCriteria");

            if (subCriteria == null || !subCriteria.isArray()) {
                throw new IdInvalidException(
                        "Tiêu chuẩn không có tiêu chí con: " + detail.getCriteriaId());
            }

            // Find selected sub-criteria
            boolean found = false;
            for (JsonNode subCriterion : subCriteria) {
                String subId = subCriterion.get("id").asText();
                if (subId.equals(detail.getSelectedSubCriteriaId())) {
                    found = true;
                    int maxScore = subCriterion.get("maxScore").asInt();

                    // Validate score matches maxScore
                    if (!detail.getScore().equals(maxScore)) {
                        throw new IdInvalidException(
                                "Điểm không khớp với điểm tối đa. Tiêu chí con: " + subId
                                        + ", điểm nhập: " + detail.getScore()
                                        + ", điểm tối đa: " + maxScore);
                    }
                    break;
                }
            }

            if (!found) {
                throw new IdInvalidException(
                        "Tiêu chí con không hợp lệ: " + detail.getSelectedSubCriteriaId()
                                + " cho tiêu chuẩn: " + detail.getCriteriaId());
            }
        }

        // Check if all criteria are scored
        if (scoredCriteriaIds.size() != criteriaMap.size()) {
            throw new IdInvalidException(
                    "Vui lòng chấm điểm cho tất cả các tiêu chí. Đã chấm: "
                            + scoredCriteriaIds.size() + "/" + criteriaMap.size());
        }
    }

    // Helper: Validate total score
    private void validateTotalScore(Innovation innovation, List<ScoreCriteriaDetail> scoringDetails, Integer totalScore) {
        int calculatedTotal = scoringDetails.stream()
                .mapToInt(ScoreCriteriaDetail::getScore)
                .sum();

        if (!totalScore.equals(calculatedTotal)) {
            throw new IdInvalidException(
                    "Tổng điểm không khớp. Tổng điểm nhập: " + totalScore
                            + ", tổng điểm tính toán: " + calculatedTotal);
        }

        // Tính maxTotalScore từ scoring criteria
        InnovationDecision decision = innovation.getInnovationRound() != null 
            ? innovation.getInnovationRound().getInnovationDecision() : null;
        JsonNode scoringCriteria = decision != null ? decision.getScoringCriteria() : null;
        int maxTotalScore = calculateMaxTotalScore(scoringCriteria);

        if (totalScore < 0 || totalScore > maxTotalScore) {
            throw new IdInvalidException(
                    String.format("Tổng điểm phải từ 0 đến %d", maxTotalScore));
        }
    }

    // Helper: Validate thời gian chấm điểm
    private void validateScoringPeriod(Innovation innovation) {
        // Lấy innovation round
        InnovationRound round = innovation.getInnovationRound();
        if (round == null) {
            throw new IdInvalidException("Sáng kiến chưa được gán vào đợt sáng kiến nào");
        }

        // Lấy danh sách councils của innovation
        List<Council> councils = innovation.getCouncils();
        if (councils == null || councils.isEmpty()) {
            throw new IdInvalidException("Sáng kiến chưa được gán cho Hội đồng nào");
        }

        // Filter councils theo round hiện tại (tránh lấy councils từ đợt khác)
        List<Council> councilsInCurrentRound = councils.stream()
                .filter(council -> council.getInnovationRound() != null
                        && council.getInnovationRound().getId().equals(round.getId()))
                .collect(Collectors.toList());

        if (councilsInCurrentRound.isEmpty()) {
            throw new IdInvalidException("Sáng kiến chưa được gán cho Hội đồng nào trong đợt sáng kiến này");
        }

        // Xác định council level từ councils trong round hiện tại
        // Lấy council mà current user là thành viên VÀ có phase ACTIVE
        User currentUser = userService.getCurrentUser();
        Council relevantCouncil = councilsInCurrentRound.stream()
                .filter(council -> {
                    // Check user là thành viên
                    List<CouncilMember> members = councilMemberRepository.findByCouncilId(council.getId());
                    boolean isMember = members.stream()
                            .anyMatch(member -> member.getUser().getId().equals(currentUser.getId()));
                    return isMember && isScoringPhaseActive(council, innovation, round);
                })
                .findFirst()
                .orElseThrow(() -> new IdInvalidException(
                        "Bạn không phải là thành viên của bất kỳ hội đồng nào đang trong thời gian chấm điểm cho sáng kiến này"));

        // Lấy phase và validate
        ReviewLevelEnum councilLevel = relevantCouncil.getReviewCouncilLevel();
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        PhaseStatusEnum phaseStatus;

        if (councilLevel == ReviewLevelEnum.KHOA) {
            Department department = getDepartmentForCouncil(relevantCouncil, innovation);
            if (department == null) {
                throw new IdInvalidException("Không xác định được khoa của sáng kiến cho hội đồng cấp khoa");
            }

            DepartmentPhase scoringPhase = departmentPhaseRepository
                    .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                            department.getId(),
                            round.getId(),
                            InnovationPhaseTypeEnum.SCORING)
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy giai đoạn chấm điểm cho khoa và đợt sáng kiến này"));

            phaseStatus = scoringPhase.getPhaseStatus();
            startDate = scoringPhase.getPhaseStartDate();
            endDate = scoringPhase.getPhaseEndDate();
        } else {
            InnovationPhase scoringPhase = innovationPhaseRepository
                    .findByInnovationRoundIdAndPhaseType(round.getId(), InnovationPhaseTypeEnum.SCORING)
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy giai đoạn chấm điểm cấp trường cho đợt sáng kiến này"));

            if (scoringPhase.getLevel() != InnovationPhaseLevelEnum.SCHOOL) {
                throw new IdInvalidException("Giai đoạn chấm điểm không phải cấp trường");
            }

            phaseStatus = scoringPhase.getPhaseStatus();
            startDate = scoringPhase.getPhaseStartDate();
            endDate = scoringPhase.getPhaseEndDate();
        }

        // Kiểm tra phase status phải là ACTIVE
        if (phaseStatus != PhaseStatusEnum.ACTIVE) {
            throw new IdInvalidException(
                    "Giai đoạn chấm điểm chưa được kích hoạt. Trạng thái hiện tại: " + phaseStatus);
        }

        // Kiểm tra thời gian hiện tại có nằm trong khoảng chấm điểm không
        if (startDate == null || endDate == null) {
            throw new IdInvalidException("Giai đoạn chấm điểm chưa có thời gian bắt đầu/kết thúc");
        }

        if (currentDate.isBefore(startDate)) {
            throw new IdInvalidException(
                    "Chưa đến thời gian chấm điểm. Thời gian chấm điểm bắt đầu từ: " + startDate);
        }

        if (currentDate.isAfter(endDate)) {
            throw new IdInvalidException(
                    "Đã hết thời gian chấm điểm. Thời gian chấm điểm kết thúc vào: " + endDate);
        }

        logger.info("Validation passed: Current date {} is within scoring period {} - {} for council level {}",
                currentDate, startDate, endDate, councilLevel);
    }

    // Helper: Check xem scoring phase có ACTIVE không
    private boolean isScoringPhaseActive(Council council, Innovation innovation, InnovationRound round) {
        ReviewLevelEnum level = council.getReviewCouncilLevel();
        if (level == ReviewLevelEnum.KHOA) {
            Department department = getDepartmentForCouncil(council, innovation);
            if (department == null) {
                return false;
            }
            Optional<DepartmentPhase> phaseOpt = departmentPhaseRepository
                    .findByDepartmentIdAndInnovationRoundIdAndPhaseType(
                            department.getId(),
                            round.getId(),
                            InnovationPhaseTypeEnum.SCORING);
            return phaseOpt.isPresent() && phaseOpt.get().getPhaseStatus() == PhaseStatusEnum.ACTIVE;
        } else {
            Optional<InnovationPhase> phaseOpt = innovationPhaseRepository
                    .findByInnovationRoundIdAndPhaseType(round.getId(), InnovationPhaseTypeEnum.SCORING);
            if (phaseOpt.isEmpty()) {
                return false;
            }
            InnovationPhase phase = phaseOpt.get();
            return phase.getLevel() == InnovationPhaseLevelEnum.SCHOOL
                    && phase.getPhaseStatus() == PhaseStatusEnum.ACTIVE;
        }
    }

    // Helper: Lấy department cho council (ưu tiên từ council, sau đó từ innovation)
    private Department getDepartmentForCouncil(Council council, Innovation innovation) {
        Department department = council.getDepartment();
        if (department == null) {
            department = innovation.getDepartment();
            if (department == null && innovation.getUser() != null) {
                department = innovation.getUser().getDepartment();
            }
        }
        return department;
    }
}
