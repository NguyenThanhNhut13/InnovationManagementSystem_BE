package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ReviewScoreRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ReviewScoreResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReviewScoreRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewScoreService {

    private final ReviewScoreRepository reviewScoreRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final InnovationRepository innovationRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;

    public List<ReviewScoreResponseDTO> getAllReviewScores() {
        return reviewScoreRepository.findAll().stream()
                .map(ReviewScoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ReviewScoreResponseDTO getReviewScoreById(UUID id) {
        ReviewScore reviewScore = reviewScoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review score not found with id: " + id));
        return new ReviewScoreResponseDTO(reviewScore);
    }

    public List<ReviewScoreResponseDTO> getReviewScoresByInnovationId(UUID innovationId) {
        return reviewScoreRepository.findByInnovationId(innovationId).stream()
                .map(ReviewScoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<ReviewScoreResponseDTO> getReviewScoresByCouncilMemberId(UUID councilMemberId) {
        return reviewScoreRepository.findByCouncilMemberId(councilMemberId).stream()
                .map(ReviewScoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<ReviewScoreResponseDTO> getReviewScoresByDecisionId(UUID decisionId) {
        return reviewScoreRepository.findByInnovationDecisionId(decisionId).stream()
                .map(ReviewScoreResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ReviewScoreResponseDTO createReviewScore(ReviewScoreRequestDTO requestDTO) {
        CouncilMember councilMember = councilMemberRepository.findById(requestDTO.getCouncilMemberId())
                .orElseThrow(() -> new RuntimeException(
                        "Council member not found with id: " + requestDTO.getCouncilMemberId()));

        Innovation innovation = innovationRepository.findById(requestDTO.getInnovationId())
                .orElseThrow(
                        () -> new RuntimeException("Innovation not found with id: " + requestDTO.getInnovationId()));

        ReviewScore reviewScore = new ReviewScore();
        reviewScore.setContent(requestDTO.getContent());
        reviewScore.setScoreLevel(requestDTO.getScoreLevel());
        reviewScore.setActualScore(requestDTO.getActualScore());
        reviewScore.setCouncilMember(councilMember);
        reviewScore.setInnovation(innovation);
        reviewScore.setCreatedBy(requestDTO.getCreatedBy());
        reviewScore.setUpdatedBy(requestDTO.getUpdatedBy());

        // Set decision if provided
        if (requestDTO.getDecisionId() != null) {
            InnovationDecision decision = innovationDecisionRepository.findById(requestDTO.getDecisionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Innovation decision not found with id: " + requestDTO.getDecisionId()));
            reviewScore.setInnovationDecision(decision);
        }

        ReviewScore savedReviewScore = reviewScoreRepository.save(reviewScore);
        return new ReviewScoreResponseDTO(savedReviewScore);
    }

    public ReviewScoreResponseDTO updateReviewScore(UUID id, ReviewScoreRequestDTO requestDTO) {
        ReviewScore reviewScore = reviewScoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review score not found with id: " + id));

        CouncilMember councilMember = councilMemberRepository.findById(requestDTO.getCouncilMemberId())
                .orElseThrow(() -> new RuntimeException(
                        "Council member not found with id: " + requestDTO.getCouncilMemberId()));

        Innovation innovation = innovationRepository.findById(requestDTO.getInnovationId())
                .orElseThrow(
                        () -> new RuntimeException("Innovation not found with id: " + requestDTO.getInnovationId()));

        reviewScore.setContent(requestDTO.getContent());
        reviewScore.setScoreLevel(requestDTO.getScoreLevel());
        reviewScore.setActualScore(requestDTO.getActualScore());
        reviewScore.setCouncilMember(councilMember);
        reviewScore.setInnovation(innovation);
        reviewScore.setUpdatedBy(requestDTO.getUpdatedBy());

        // Set decision if provided
        if (requestDTO.getDecisionId() != null) {
            InnovationDecision decision = innovationDecisionRepository.findById(requestDTO.getDecisionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Innovation decision not found with id: " + requestDTO.getDecisionId()));
            reviewScore.setInnovationDecision(decision);
        } else {
            reviewScore.setInnovationDecision(null);
        }

        ReviewScore updatedReviewScore = reviewScoreRepository.save(reviewScore);
        return new ReviewScoreResponseDTO(updatedReviewScore);
    }

    public void deleteReviewScore(UUID id) {
        if (!reviewScoreRepository.existsById(id)) {
            throw new RuntimeException("Review score not found with id: " + id);
        }
        reviewScoreRepository.deleteById(id);
    }
}