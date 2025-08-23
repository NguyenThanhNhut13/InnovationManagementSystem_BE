package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.InnovationRoundRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.InnovationRoundResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationDecisionRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

@Service
public class InnovationRoundService {

    private final InnovationRoundRepository innovationRoundRepository;
    private final InnovationDecisionRepository innovationDecisionRepository;

    public InnovationRoundService(InnovationRoundRepository innovationRoundRepository,
            InnovationDecisionRepository innovationDecisionRepository) {
        this.innovationRoundRepository = innovationRoundRepository;
        this.innovationDecisionRepository = innovationDecisionRepository;
    }

    // 1. Create Innovation Round
    @Transactional
    public InnovationRoundResponse createInnovationRound(InnovationRoundRequest request) {

        InnovationDecision innovationDecision = innovationDecisionRepository.findById(request.getInnovationDecisionId())
                .orElseThrow(() -> new IdInvalidException("ID Quyết định sáng tạo không tồn tại"));
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IdInvalidException("Ngày kết thúc phải sau hơn ngày bắt đầu");
        }

        InnovationRound innovationRound = new InnovationRound();
        innovationRound.setName(request.getName());
        innovationRound.setStartDate(request.getStartDate());
        innovationRound.setEndDate(request.getEndDate());
        innovationRound.setStatus(InnovationRoundStatusEnum.ACTIVE);
        innovationRound.setInnovationDecision(innovationDecision);

        innovationRoundRepository.save(innovationRound);
        return toInnovationRoundResponse(innovationRound);
    }

    // 2. Get All InnovationRounds with pagination and filter
    public ResultPaginationDTO getAllInnovationRounds(Specification<InnovationRound> specification, Pageable pageable) {
        Page<InnovationRound> innovationRounds = innovationRoundRepository.findAll(specification, pageable);

        Page<InnovationRoundResponse> responses = innovationRounds.map(this::toInnovationRoundResponse);

        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 3. Get InnovationRound by Id
    public InnovationRoundResponse getInnovationRoundById(String id) {
        InnovationRound innovationRound = innovationRoundRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ID quyết định sáng tạo không tồn tại"));
        return toInnovationRoundResponse(innovationRound);
    }

    // 4. Update InnovationRound
    @Transactional
    public InnovationRoundResponse updateInnovationRound(String id, InnovationRoundRequest request) {

        InnovationRound innovationRound = innovationRoundRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ID đợt sáng kiến không tồn tại"));
        InnovationDecision innovationDecision = innovationDecisionRepository.findById(request.getInnovationDecisionId())
                .orElseThrow(() -> new IdInvalidException("ID quyết định của sáng kiến không tồn tại"));
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IdInvalidException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        innovationRound.setName(request.getName());
        innovationRound.setStartDate(request.getStartDate());
        innovationRound.setEndDate(request.getEndDate());
        innovationRound.setInnovationDecision(innovationDecision);
        innovationRoundRepository.save(innovationRound);
        return toInnovationRoundResponse(innovationRound);
    }

    // 5. Change Status InnovationRound
    @Transactional
    public InnovationRoundResponse changeStatusInnovationRound(String id, InnovationRoundStatusEnum newStatus) {
        InnovationRound innovationRound = innovationRoundRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("ID đợt sáng kiến không tồn tại"));
        if (newStatus == null) {
            throw new IdInvalidException("Trạng thái đợt sáng kiến không được để trống");
        }
        innovationRound.setStatus(newStatus);
        innovationRoundRepository.save(innovationRound);
        return toInnovationRoundResponse(innovationRound);
    }

    // 6. Get InnovationRound by Status
    public ResultPaginationDTO getInnovationRoundByStatus(InnovationRoundStatusEnum status, Pageable pageable) {

        Page<InnovationRound> pageInnovationRound = innovationRoundRepository.findByStatus(status, pageable);

        Page<InnovationRoundResponse> innovationRoundResponses = pageInnovationRound
                .map(this::toInnovationRoundResponse);

        return Utils.toResultPaginationDTO(innovationRoundResponses, pageable);
    }

    // Mapper
    private InnovationRoundResponse toInnovationRoundResponse(InnovationRound innovationRound) {
        InnovationRoundResponse response = new InnovationRoundResponse();
        response.setId(innovationRound.getId());
        response.setName(innovationRound.getName());
        response.setStartDate(innovationRound.getStartDate());
        response.setEndDate(innovationRound.getEndDate());
        response.setStatus(innovationRound.getStatus());
        response.setCreatedAt(innovationRound.getCreatedAt());
        response.setUpdatedAt(innovationRound.getUpdatedAt());
        response.setCreatedBy(innovationRound.getCreatedBy());
        response.setUpdatedBy(innovationRound.getUpdatedBy());
        response.setInnovationDecisionId(innovationRound.getInnovationDecision().getId());

        return response;
    }
}
