package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationDecisionRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationDecisionResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationDecisionMapper;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class InnovationDecisionService {

    private final InnovationDecisionRepository innovationDecisionRepository;
    private final InnovationDecisionMapper innovationDecisionMapper;

    public InnovationDecisionService(InnovationDecisionRepository innovationDecisionRepository,
            InnovationDecisionMapper innovationDecisionMapper) {
        this.innovationDecisionRepository = innovationDecisionRepository;
        this.innovationDecisionMapper = innovationDecisionMapper;
    }

    // 1. Tạo InnovationDecision
    @Transactional
    public InnovationDecisionResponse createInnovationDecision(InnovationDecisionRequest request) {
        if (innovationDecisionRepository.existsByDecisionNumber(request.getDecisionNumber())) {
            throw new IdInvalidException("Số hiệu quyết định đã tồn tại");
        }

        InnovationDecision innovationDecision = innovationDecisionMapper.toInnovationDecision(request);

        innovationDecisionRepository.save(innovationDecision);
        return innovationDecisionMapper.toInnovationDecisionResponse(innovationDecision);
    }

    // 2. Tạo Decision
    @Transactional
    public InnovationDecision createDecision(InnovationDecisionRequest req) {

        if (innovationDecisionRepository.existsByDecisionNumber(req.getDecisionNumber())) {
            throw new IdInvalidException("Số hiệu quyết định đã tồn tại");
        }

        InnovationDecision decision = new InnovationDecision();
        decision.setDecisionNumber(req.getDecisionNumber());
        decision.setTitle(req.getTitle());
        decision.setPromulgatedDate(req.getPromulgatedDate());
        decision.setFileName(req.getFileName());
        decision.setScoringCriteria(req.getScoringCriteria());
        decision.setContentGuide(req.getContentGuide());

        return innovationDecisionRepository.save(decision);
    }

    // 3. Lấy tất cả InnovationDecisions
    public ResultPaginationDTO getAllInnovationDecisions(Specification<InnovationDecision> specification,
            Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<InnovationDecision> innovationDecisions = innovationDecisionRepository.findAll(specification, pageable);
        Page<InnovationDecisionResponse> responses = innovationDecisions
                .map(innovationDecisionMapper::toInnovationDecisionResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 4. Lấy InnovationDecision by Id
    public InnovationDecisionResponse getInnovationDecisionById(String id) {
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));
        return innovationDecisionMapper.toInnovationDecisionResponse(innovationDecision);
    }

    // 5. Update InnovationDecision
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

        innovationDecisionRepository.save(innovationDecision);
        return innovationDecisionMapper.toInnovationDecisionResponse(innovationDecision);
    }

    // 7. Lấy by date range
    public ResultPaginationDTO getInnovationDecisionsByDateRange(LocalDate startDate, LocalDate endDate,
            Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<InnovationDecision> innovationDecisions = innovationDecisionRepository
                .findByPromulgatedDateBetween(startDate, endDate, pageable);
        Page<InnovationDecisionResponse> responses = innovationDecisions
                .map(innovationDecisionMapper::toInnovationDecisionResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 7. Lấy Entity By ID
    public Optional<InnovationDecision> getEntityById(String id) {
        return innovationDecisionRepository.findById(id);
    }
}
