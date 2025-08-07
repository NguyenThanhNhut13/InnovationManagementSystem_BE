package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewComment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ReviewCommentRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ReviewCommentResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReviewCommentRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewCommentService {

    private final ReviewCommentRepository reviewCommentRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final InnovationRepository innovationRepository;

    public List<ReviewCommentResponseDTO> getAllReviewComments() {
        return reviewCommentRepository.findAll().stream()
                .map(ReviewCommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ReviewCommentResponseDTO getReviewCommentById(UUID id) {
        ReviewComment reviewComment = reviewCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review comment not found with id: " + id));
        return new ReviewCommentResponseDTO(reviewComment);
    }

    public List<ReviewCommentResponseDTO> getReviewCommentsByInnovationId(UUID innovationId) {
        return reviewCommentRepository.findByInnovationId(innovationId).stream()
                .map(ReviewCommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<ReviewCommentResponseDTO> getReviewCommentsByCouncilMemberId(UUID councilMemberId) {
        return reviewCommentRepository.findByCouncilMemberId(councilMemberId).stream()
                .map(ReviewCommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ReviewCommentResponseDTO createReviewComment(ReviewCommentRequestDTO requestDTO) {
        CouncilMember councilMember = councilMemberRepository.findById(requestDTO.getCouncilMemberId())
                .orElseThrow(() -> new RuntimeException(
                        "Council member not found with id: " + requestDTO.getCouncilMemberId()));

        Innovation innovation = innovationRepository.findById(requestDTO.getInnovationId())
                .orElseThrow(
                        () -> new RuntimeException("Innovation not found with id: " + requestDTO.getInnovationId()));

        ReviewComment reviewComment = new ReviewComment();
        reviewComment.setComment(requestDTO.getComment());
        reviewComment.setReviewsLevel(requestDTO.getReviewsLevel());
        reviewComment.setInnovation(innovation);
        reviewComment.setCouncilMember(councilMember);

        ReviewComment savedReviewComment = reviewCommentRepository.save(reviewComment);
        return new ReviewCommentResponseDTO(savedReviewComment);
    }

    public ReviewCommentResponseDTO updateReviewComment(UUID id, ReviewCommentRequestDTO requestDTO) {
        ReviewComment reviewComment = reviewCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review comment not found with id: " + id));

        CouncilMember councilMember = councilMemberRepository.findById(requestDTO.getCouncilMemberId())
                .orElseThrow(() -> new RuntimeException(
                        "Council member not found with id: " + requestDTO.getCouncilMemberId()));

        Innovation innovation = innovationRepository.findById(requestDTO.getInnovationId())
                .orElseThrow(
                        () -> new RuntimeException("Innovation not found with id: " + requestDTO.getInnovationId()));

        reviewComment.setComment(requestDTO.getComment());
        reviewComment.setReviewsLevel(requestDTO.getReviewsLevel());
        reviewComment.setInnovation(innovation);
        reviewComment.setCouncilMember(councilMember);

        ReviewComment updatedReviewComment = reviewCommentRepository.save(reviewComment);
        return new ReviewCommentResponseDTO(updatedReviewComment);
    }

    public void deleteReviewComment(UUID id) {
        if (!reviewCommentRepository.existsById(id)) {
            throw new RuntimeException("Review comment not found with id: " + id);
        }
        reviewCommentRepository.deleteById(id);
    }
}