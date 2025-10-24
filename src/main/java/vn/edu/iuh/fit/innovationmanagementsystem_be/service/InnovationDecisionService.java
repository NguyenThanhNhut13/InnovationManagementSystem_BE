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

    // 1. Tạo InnovationDecision - Using InnovationRoundService
    @Transactional
    public InnovationDecision createDecision(InnovationDecisionRequest req) {

        InnovationDecision decision = new InnovationDecision();
        decision.setDecisionNumber(req.getDecisionNumber());
        decision.setTitle(req.getTitle());
        decision.setPromulgatedDate(req.getPromulgatedDate());
        decision.setFileName(req.getFileName());
        decision.setScoringCriteria(req.getScoringCriteria());
        decision.setContentGuide(req.getContentGuide());

        return innovationDecisionRepository.save(decision);
    }

    // 2. Lấy tất cả InnovationDecisions với pagination và filtering
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

    // 3. Lấy InnovationDecision by Id - Using InnovationRoundService
    public InnovationDecisionResponse getInnovationDecisionById(String id) {
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Quyết định không tồn tại"));
        return innovationDecisionMapper.toInnovationDecisionResponse(innovationDecision);
    }

    // 4. Lấy Entity By ID
    public Optional<InnovationDecision> getEntityById(String id) {
        return innovationDecisionRepository.findById(id);
    }
}
