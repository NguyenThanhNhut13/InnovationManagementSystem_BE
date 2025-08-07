package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ReviewCommentRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ReviewCommentResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ReviewCommentService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/review-comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewCommentController {

    private final ReviewCommentService reviewCommentService;

    @GetMapping
    public ResponseEntity<List<ReviewCommentResponseDTO>> getAllReviewComments() {
        List<ReviewCommentResponseDTO> reviewComments = reviewCommentService.getAllReviewComments();
        return ResponseEntity.ok(reviewComments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewCommentResponseDTO> getReviewCommentById(@PathVariable UUID id) {
        ReviewCommentResponseDTO reviewComment = reviewCommentService.getReviewCommentById(id);
        return ResponseEntity.ok(reviewComment);
    }

    @GetMapping("/innovation/{innovationId}")
    public ResponseEntity<List<ReviewCommentResponseDTO>> getReviewCommentsByInnovationId(
            @PathVariable UUID innovationId) {
        List<ReviewCommentResponseDTO> reviewComments = reviewCommentService
                .getReviewCommentsByInnovationId(innovationId);
        return ResponseEntity.ok(reviewComments);
    }

    @GetMapping("/council-member/{councilMemberId}")
    public ResponseEntity<List<ReviewCommentResponseDTO>> getReviewCommentsByCouncilMemberId(
            @PathVariable UUID councilMemberId) {
        List<ReviewCommentResponseDTO> reviewComments = reviewCommentService
                .getReviewCommentsByCouncilMemberId(councilMemberId);
        return ResponseEntity.ok(reviewComments);
    }

    @PostMapping
    public ResponseEntity<ReviewCommentResponseDTO> createReviewComment(
            @Valid @RequestBody ReviewCommentRequestDTO requestDTO) {
        ReviewCommentResponseDTO createdReviewComment = reviewCommentService.createReviewComment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReviewComment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewCommentResponseDTO> updateReviewComment(@PathVariable UUID id,
            @Valid @RequestBody ReviewCommentRequestDTO requestDTO) {
        ReviewCommentResponseDTO updatedReviewComment = reviewCommentService.updateReviewComment(id, requestDTO);
        return ResponseEntity.ok(updatedReviewComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReviewComment(@PathVariable UUID id) {
        reviewCommentService.deleteReviewComment(id);
        return ResponseEntity.noContent().build();
    }
}