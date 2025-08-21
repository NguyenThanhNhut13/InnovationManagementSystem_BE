package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.InnovationDecisionRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.InnovationDecisionResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class InnovationDecisionService {

    private final InnovationDecisionRepository innovationDecisionRepository;

    public InnovationDecisionService(InnovationDecisionRepository innovationDecisionRepository) {
        this.innovationDecisionRepository = innovationDecisionRepository;
    }

    // 1. Create InnovationDecision
    @Transactional
    public InnovationDecisionResponse createInnovationDecision(InnovationDecisionRequest request) {
        if (innovationDecisionRepository.existsByDecisionNumber(request.getDecisionNumber())) {
            throw new IdInvalidException("Số hiệu quyết định đã tồn tại");
        }

        InnovationDecision innovationDecision = new InnovationDecision();
        innovationDecision.setDecisionNumber(request.getDecisionNumber());
        innovationDecision.setTitle(request.getTitle());
        innovationDecision.setPromulgatedDate(request.getPromulgatedDate());
        innovationDecision.setSignedBy(request.getSignedBy());
        innovationDecision.setBases(request.getBases());

        innovationDecisionRepository.save(innovationDecision);
        return toInnovationDecisionResponse(innovationDecision);
    }

    // 2. Get All InnovationDecisions
    public ResultPaginationDTO getAllInnovationDecisions(Specification<InnovationDecision> specification,
            Pageable pageable) {
        Page<InnovationDecision> innovationDecisions = innovationDecisionRepository.findAll(specification, pageable);
        Page<InnovationDecisionResponse> responses = innovationDecisions.map(this::toInnovationDecisionResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 3. Get InnovationDecision by Id
    public InnovationDecisionResponse getInnovationDecisionById(String id) {
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));
        return toInnovationDecisionResponse(innovationDecision);
    }

    // 4. Update InnovationDecision
    @Transactional
    public InnovationDecisionResponse updateInnovationDecision(String id, InnovationDecisionRequest request) {
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy quyết định có ID: " + id));

        if (request.getDecisionNumber() != null
                && !request.getDecisionNumber().equals(innovationDecision.getDecisionNumber())) {
            if (innovationDecisionRepository.existsByDecisionNumber(request.getDecisionNumber())) {
                throw new IdInvalidException("Số hiệu quyết định đã tồn tại");
            }
            innovationDecision.setDecisionNumber(request.getDecisionNumber());
        }

        if (request.getTitle() != null) {
            innovationDecision.setTitle(request.getTitle());
        }
        if (request.getPromulgatedDate() != null) {
            innovationDecision.setPromulgatedDate(request.getPromulgatedDate());
        }
        if (request.getSignedBy() != null) {
            innovationDecision.setSignedBy(request.getSignedBy());
        }
        if (request.getBases() != null) {
            innovationDecision.setBases(request.getBases());
        }

        innovationDecisionRepository.save(innovationDecision);
        return toInnovationDecisionResponse(innovationDecision);
    }

    // 5. Get by signed by
    public ResultPaginationDTO getInnovationDecisionsBySignedBy(String signedBy, Pageable pageable) {
        Page<InnovationDecision> innovationDecisions = innovationDecisionRepository
                .findBySignedBy(signedBy, pageable);
        Page<InnovationDecisionResponse> responses = innovationDecisions.map(this::toInnovationDecisionResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 6. Get by date range
    public ResultPaginationDTO getInnovationDecisionsByDateRange(LocalDate startDate, LocalDate endDate,
            Pageable pageable) {
        Page<InnovationDecision> innovationDecisions = innovationDecisionRepository
                .findByPromulgatedDateBetween(startDate, endDate, pageable);
        Page<InnovationDecisionResponse> responses = innovationDecisions.map(this::toInnovationDecisionResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // Convert to Response DTO
    private InnovationDecisionResponse toInnovationDecisionResponse(InnovationDecision innovationDecision) {
        InnovationDecisionResponse response = new InnovationDecisionResponse();
        response.setId(innovationDecision.getId());
        response.setDecisionNumber(innovationDecision.getDecisionNumber());
        response.setTitle(innovationDecision.getTitle());
        response.setPromulgatedDate(innovationDecision.getPromulgatedDate());
        response.setSignedBy(innovationDecision.getSignedBy());
        response.setBases(innovationDecision.getBases());
        response.setCreatedAt(innovationDecision.getCreatedAt());
        response.setUpdatedAt(innovationDecision.getUpdatedAt());
        response.setCreatedBy(innovationDecision.getCreatedBy());
        response.setUpdatedBy(innovationDecision.getUpdatedBy());

        if (innovationDecision.getChapters() != null) {
            response.setChapterIds(innovationDecision.getChapters().stream()
                    .map(chapter -> chapter.getId())
                    .collect(Collectors.toList()));
        }
        if (innovationDecision.getRegulations() != null) {
            response.setRegulationIds(innovationDecision.getRegulations().stream()
                    .map(regulation -> regulation.getId())
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
