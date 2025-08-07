package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ReviewScoreRequestDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ReviewScoreResponseDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ReviewScoreService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/review-scores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewScoreController {

    private final ReviewScoreService reviewScoreService;

    @GetMapping
    public ResponseEntity<List<ReviewScoreResponseDTO>> getAllReviewScores() {
        List<ReviewScoreResponseDTO> reviewScores = reviewScoreService.getAllReviewScores();
        return ResponseEntity.ok(reviewScores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewScoreResponseDTO> getReviewScoreById(@PathVariable UUID id) {
        ReviewScoreResponseDTO reviewScore = reviewScoreService.getReviewScoreById(id);
        return ResponseEntity.ok(reviewScore);
    }

    @GetMapping("/innovation/{innovationId}")
    public ResponseEntity<List<ReviewScoreResponseDTO>> getReviewScoresByInnovationId(@PathVariable UUID innovationId) {
        List<ReviewScoreResponseDTO> reviewScores = reviewScoreService.getReviewScoresByInnovationId(innovationId);
        return ResponseEntity.ok(reviewScores);
    }

    @GetMapping("/council-member/{councilMemberId}")
    public ResponseEntity<List<ReviewScoreResponseDTO>> getReviewScoresByCouncilMemberId(
            @PathVariable UUID councilMemberId) {
        List<ReviewScoreResponseDTO> reviewScores = reviewScoreService
                .getReviewScoresByCouncilMemberId(councilMemberId);
        return ResponseEntity.ok(reviewScores);
    }

    @GetMapping("/decision/{decisionId}")
    public ResponseEntity<List<ReviewScoreResponseDTO>> getReviewScoresByDecisionId(@PathVariable UUID decisionId) {
        List<ReviewScoreResponseDTO> reviewScores = reviewScoreService.getReviewScoresByDecisionId(decisionId);
        return ResponseEntity.ok(reviewScores);
    }

    @PostMapping
    public ResponseEntity<ReviewScoreResponseDTO> createReviewScore(
            @Valid @RequestBody ReviewScoreRequestDTO requestDTO) {
        ReviewScoreResponseDTO createdReviewScore = reviewScoreService.createReviewScore(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReviewScore);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewScoreResponseDTO> updateReviewScore(@PathVariable UUID id,
            @Valid @RequestBody ReviewScoreRequestDTO requestDTO) {
        ReviewScoreResponseDTO updatedReviewScore = reviewScoreService.updateReviewScore(id, requestDTO);
        return ResponseEntity.ok(updatedReviewScore);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReviewScore(@PathVariable UUID id) {
        reviewScoreService.deleteReviewScore(id);
        return ResponseEntity.noContent().build();
    }
}