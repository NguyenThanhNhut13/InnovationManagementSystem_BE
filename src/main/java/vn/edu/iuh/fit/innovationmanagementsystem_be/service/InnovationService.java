package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.InnovationFormDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.InnovationMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InnovationService {

    private final InnovationRepository innovationRepository;
    private final InnovationRoundRepository innovationRoundRepository;
    private final FormDataService formDataService;
    private final InnovationMapper innovationMapper;
    private final UserService userService;

    public InnovationService(InnovationRepository innovationRepository,
            InnovationRoundRepository innovationRoundRepository,
            FormDataService formDataService,
            InnovationMapper innovationMapper,
            UserService userService) {
        this.innovationRepository = innovationRepository;
        this.innovationRoundRepository = innovationRoundRepository;
        this.formDataService = formDataService;
        this.innovationMapper = innovationMapper;
        this.userService = userService;
    }

    // 1. Get All Innovations
    public ResultPaginationDTO getAllInnovations(Specification<Innovation> specification, Pageable pageable) {
        Page<Innovation> innovations = innovationRepository.findAll(specification, pageable);
        Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

    // 2. Get Innovation by Id
    public InnovationResponse getInnovationById(String id) {
        Innovation innovation = innovationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + id));
        return innovationMapper.toInnovationResponse(innovation);
    }

    // 3. Create Innovation & Submit Form Data (Tạo sáng kiến tự động khi điền form)
    public InnovationFormDataResponse createInnovationAndSubmitFormData(InnovationFormDataRequest request) {

        InnovationRound innovationRound = innovationRoundRepository.findByIdWithDecision(request.getInnovationRoundId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy đợt sáng kiến với ID: " + request.getInnovationRoundId()));

        User currentUser = userService.getCurrentUser();

        Innovation innovation = new Innovation();
        innovation.setInnovationName(request.getInnovationName());
        innovation.setUser(currentUser);
        innovation.setDepartment(currentUser.getDepartment());
        innovation.setInnovationRound(innovationRound);
        innovation.setStatus(InnovationStatusEnum.DRAFT);
        innovation.setIsScore(request.getIsScore() != null ? request.getIsScore() : false);

        Innovation savedInnovation = innovationRepository.save(innovation);

        List<FormDataResponse> formDataResponses = request.getFormDataItems().stream()
                .map(item -> {
                    FormDataRequest createRequest = new FormDataRequest();
                    createRequest.setFieldValue(item.getFieldValue());
                    createRequest.setFormFieldId(item.getFormFieldId());
                    createRequest.setInnovationId(savedInnovation.getId());
                    return formDataService.createFormData(createRequest);
                })
                .collect(Collectors.toList());

        String actionType = request.getActionType() != null ? request.getActionType().toUpperCase() : "DRAFT";

        if (InnovationStatusEnum.SUBMITTED.name().equals(actionType)) {
            savedInnovation.setStatus(InnovationStatusEnum.SUBMITTED);
            innovationRepository.save(savedInnovation);
        }

        InnovationFormDataResponse response = new InnovationFormDataResponse();
        response.setInnovation(innovationMapper.toInnovationResponse(savedInnovation));
        response.setFormDataList(formDataResponses);

        return response;
    }

    // 4. Get Innovation Form Data
    public InnovationFormDataResponse getInnovationFormData(String innovationId, String templateId) {

        Innovation innovation = innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));

        if (!userService.isOwnerOfInnovation(innovation.getUser().getId())) {
            throw new IdInvalidException("Bạn không có quyền xem thông tin sáng kiến này");
        }

        List<FormDataResponse> formDataList;
        if (templateId != null) {
            formDataList = formDataService.getFormDataWithFormFields(innovationId, templateId);
        } else {
            formDataList = formDataService.getFormDataByInnovationId(innovationId);
        }

        InnovationFormDataResponse response = new InnovationFormDataResponse();
        response.setInnovation(innovationMapper.toInnovationResponse(innovation));
        response.setFormDataList(formDataList);

        return response;
    }

    // 5-6. Get Innovations by User and Status
    public ResultPaginationDTO getInnovationsByUserAndStatus(String status, Pageable pageable) {
        String currentUserId = userService.getCurrentUserId();
        InnovationStatusEnum statusEnum = InnovationStatusEnum.valueOf(status);
        Page<Innovation> innovations = innovationRepository.findByUserIdAndStatus(currentUserId, statusEnum,
                pageable);
        Page<InnovationResponse> responses = innovations.map(innovationMapper::toInnovationResponse);
        return Utils.toResultPaginationDTO(responses, pageable);
    }

}
