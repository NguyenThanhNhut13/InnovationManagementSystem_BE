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
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReviewScoreRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ReviewScoreService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewScoreService.class);

    private final ReviewScoreRepository reviewScoreRepository;
    private final InnovationRepository innovationRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserService userService;
    private final ReviewScoreMapper reviewScoreMapper;
    private final ObjectMapper objectMapper;

    public ReviewScoreService(
            ReviewScoreRepository reviewScoreRepository,
            InnovationRepository innovationRepository,
            CouncilMemberRepository councilMemberRepository,
            UserService userService,
            ReviewScoreMapper reviewScoreMapper,
            ObjectMapper objectMapper) {
        this.reviewScoreRepository = reviewScoreRepository;
        this.innovationRepository = innovationRepository;
        this.councilMemberRepository = councilMemberRepository;
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

        // 5. Validate scoring details match với scoring criteria
        validateScoringDetails(innovation, request.getScoringDetails());

        // 6. Validate total score
        validateTotalScore(request.getScoringDetails(), request.getTotalScore());

        // 6. Check if user đã chấm điểm chưa
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

        // 7. Set scoring data
        reviewScore.setScoringDetails(objectMapper.valueToTree(request.getScoringDetails()));
        reviewScore.setTotalScore(request.getTotalScore());
        reviewScore.setIsApproved(request.getIsApproved());
        reviewScore.setRequiresSupplementaryDocuments(
                request.getRequiresSupplementaryDocuments() != null
                        ? request.getRequiresSupplementaryDocuments()
                        : false);
        reviewScore.setDetailedComments(request.getDetailedComments());
        reviewScore.setReviewedAt(LocalDateTime.now());

        // 8. Save
        ReviewScore savedScore = reviewScoreRepository.save(reviewScore);

        logger.info("Score submitted successfully for innovation: {}", innovationId);

        // 9. Return response
        return reviewScoreMapper.toInnovationScoreResponse(savedScore);
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
                    "Bạn không phải là thành viên Hội đồng được gán cho sáng kiến này. " +
                            "Chỉ thành viên Hội đồng mới có quyền chấm điểm");
        }

        logger.info("User {} is a valid council member for innovation {}", user.getId(), innovation.getId());
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
    private void validateTotalScore(List<ScoreCriteriaDetail> scoringDetails, Integer totalScore) {
        int calculatedTotal = scoringDetails.stream()
                .mapToInt(ScoreCriteriaDetail::getScore)
                .sum();

        if (!totalScore.equals(calculatedTotal)) {
            throw new IdInvalidException(
                    "Tổng điểm không khớp. Tổng điểm nhập: " + totalScore
                            + ", tổng điểm tính toán: " + calculatedTotal);
        }

        if (totalScore < 0 || totalScore > 100) {
            throw new IdInvalidException("Tổng điểm phải từ 0 đến 100");
        }
    }
}
