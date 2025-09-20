package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateInnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationRoundMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InnovationRoundService {

    private final InnovationRoundRepository innovationRoundRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;
    private final InnovationRoundMapper innovationRoundMapper;

    public InnovationRoundService(InnovationRoundRepository innovationRoundRepository,
            InnovationDecisionRepository innovationDecisionRepository,
            InnovationRoundMapper innovationRoundMapper) {
        this.innovationRoundRepository = innovationRoundRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.innovationRoundMapper = innovationRoundMapper;
    }

    // 1. Create innovation round
    public InnovationRoundResponse createInnovationRound(InnovationRoundRequest request) {
        InnovationDecision decision = innovationDecisionRepository.findById(request.getDecisionId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy InnovationDecision với ID: " + request.getDecisionId()));

        // Validate dates
        validateRoundDates(request.getStartDate(), request.getEndDate());

        InnovationRound round = innovationRoundMapper.toInnovationRound(request);
        round.setInnovationDecision(decision);
        round.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        InnovationRound savedRound = innovationRoundRepository.save(round);
        return innovationRoundMapper.toInnovationRoundResponse(savedRound);
    }

    // 2. Get all rounds by decision with pagination and filtering
    public ResultPaginationDTO getRoundsByDecision(@NonNull String decisionId,
            @NonNull Specification<InnovationRound> specification,
            @NonNull Pageable pageable) {

        // Verify decision exists
        if (!innovationDecisionRepository.existsById(decisionId)) {
            throw new IdInvalidException("Không tìm thấy InnovationDecision với ID: " + decisionId);
        }

        // Create specification that includes decisionId filter
        Specification<InnovationRound> decisionSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(root.get("innovationDecision").get("id"), decisionId);

        // Combine with provided specification
        Specification<InnovationRound> combinedSpec = decisionSpec.and(specification);

        Page<InnovationRound> rounds = innovationRoundRepository.findAll(combinedSpec, pageable);
        Page<InnovationRoundResponse> responses = rounds.map(innovationRoundMapper::toInnovationRoundResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 3. Get round by ID
    public InnovationRoundResponse getRoundById(String roundId) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));
        return innovationRoundMapper.toInnovationRoundResponse(round);
    }

    // 4. Get current active round
    public InnovationRoundResponse getCurrentActiveRound(String decisionId) {
        InnovationRound round = innovationRoundRepository.findCurrentActiveRound(decisionId, LocalDate.now())
                .orElse(null);
        return round != null ? innovationRoundMapper.toInnovationRoundResponse(round) : null;
    }

    // 5. Update round
    public InnovationRoundResponse updateRound(String roundId, UpdateInnovationRoundRequest request) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        if (request.getName() != null) {
            round.setName(request.getName());
        }
        if (request.getStartDate() != null) {
            round.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            round.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            round.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            round.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            round.setIsActive(request.getIsActive());
        }
        if (request.getAcademicYear() != null) {
            round.setAcademicYear(request.getAcademicYear());
        }

        // Validate dates if they were updated
        if (request.getStartDate() != null || request.getEndDate() != null) {
            validateRoundDates(round.getStartDate(), round.getEndDate());
        }

        InnovationRound savedRound = innovationRoundRepository.save(round);
        return innovationRoundMapper.toInnovationRoundResponse(savedRound);
    }

    // 7. Toggle round status
    public InnovationRoundResponse toggleRoundStatus(String roundId, boolean isActive) {
        InnovationRound round = innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy InnovationRound với ID: " + roundId));

        round.setIsActive(isActive);
        InnovationRound savedRound = innovationRoundRepository.save(round);
        return innovationRoundMapper.toInnovationRoundResponse(savedRound);
    }

    // 8. Get rounds by status
    public List<InnovationRoundResponse> getRoundsByStatus(String status) {
        List<InnovationRound> rounds = innovationRoundRepository.findByStatus(
                vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum
                        .valueOf(status));
        return rounds.stream()
                .map(innovationRoundMapper::toInnovationRoundResponse)
                .collect(Collectors.toList());
    }

    // Helper method to validate round dates
    private void validateRoundDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IdInvalidException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }
}
