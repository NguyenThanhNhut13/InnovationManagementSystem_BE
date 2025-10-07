package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateMultipleFormTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateMultipleFormTemplatesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final InnovationPhaseRepository innovationPhaseRepository;
    private final FormTemplateMapper formTemplateMapper;

    public FormTemplateService(FormTemplateRepository formTemplateRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            FormTemplateMapper formTemplateMapper) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.formTemplateMapper = formTemplateMapper;
    }

    // 1. Lấy form template by id
    public FormTemplateResponse getFormTemplateById(String id) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        return formTemplateMapper.toFormTemplateResponse(template);
    }

    // 2. Lấy tất cả form templates by innovation phase (via innovation round)
    public List<FormTemplateResponse> getFormTemplatesByInnovationPhase(String phaseId) {
        // Lấy innovation phase để tìm innovation round
        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Innovation phase không tồn tại với ID: " + phaseId));

        // Lấy form templates by innovation round
        List<FormTemplate> templates = formTemplateRepository
                .findByInnovationRoundIdOrderByTemplateType(phase.getInnovationRound().getId());
        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 3. Tạo form template
    public FormTemplateResponse createFormTemplate(CreateFormTemplateRequest request) {

        InnovationPhase phase = innovationPhaseRepository.findById(request.getInnovationPhaseId())
                .orElseThrow(() -> new IdInvalidException(
                        "Innovation phase không tồn tại với ID: " + request.getInnovationPhaseId()));

        FormTemplate template = new FormTemplate();
        template.setTemplateType(request.getTemplateType());
        template.setTargetRole(request.getTargetRole());
        template.setTemplateContent(request.getTemplateContent());
        template.setInnovationRound(phase.getInnovationRound());

        FormTemplate savedTemplate = formTemplateRepository.save(template);
        return formTemplateMapper.toFormTemplateResponse(savedTemplate);
    }

    // 4. Tạo Multiple Form Templates
    @Transactional
    public CreateMultipleFormTemplatesResponse createMultipleFormTemplates(CreateMultipleFormTemplatesRequest request) {

        InnovationPhase innovationPhase = innovationPhaseRepository.findById(request.getInnovationPhaseId())
                .orElseThrow(() -> new IdInvalidException(
                        "Innovation phase không tồn tại với ID: " + request.getInnovationPhaseId()));

        if (request.getFormTemplates() == null || request.getFormTemplates().isEmpty()) {
            throw new IdInvalidException("Danh sách form templates không được để trống");
        }

        List<TemplateTypeEnum> templateTypes = request.getFormTemplates().stream()
                .map(CreateMultipleFormTemplatesRequest.FormTemplateData::getTemplateType)
                .collect(Collectors.toList());

        if (templateTypes.size() != templateTypes.stream().distinct().count()) {
            throw new IdInvalidException("Danh sách form templates có loại template trùng lặp");
        }

        List<FormTemplate> formTemplates = request.getFormTemplates().stream()
                .map(templateData -> {
                    FormTemplate template = new FormTemplate();
                    template.setTemplateType(templateData.getTemplateType());
                    template.setTargetRole(templateData.getTargetRole());
                    template.setTemplateContent(templateData.getTemplateContent());
                    template.setInnovationRound(innovationPhase.getInnovationRound());
                    return template;
                })
                .collect(Collectors.toList());

        List<FormTemplate> savedFormTemplates = formTemplateRepository.saveAll(formTemplates);

        List<FormTemplateResponse> formTemplateResponses = savedFormTemplates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());

        return new CreateMultipleFormTemplatesResponse(
                request.getInnovationPhaseId(),
                innovationPhase.getName(),
                formTemplateResponses);
    }

    // 5. Cập nhật form template
    public FormTemplateResponse updateFormTemplate(String id, UpdateFormTemplateRequest request) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        if (request.getTemplateType() == null && request.getTargetRole() == null
                && request.getTemplateContent() == null) {
            throw new IdInvalidException("Ít nhất một trường phải được cung cấp để cập nhật");
        }

        if (request.getTemplateType() != null) {
            template.setTemplateType(request.getTemplateType());
        }
        if (request.getTargetRole() != null) {
            template.setTargetRole(request.getTargetRole());
        }
        if (request.getTemplateContent() != null) {
            template.setTemplateContent(request.getTemplateContent());
        }

        FormTemplate updatedTemplate = formTemplateRepository.save(template);
        return formTemplateMapper.toFormTemplateResponse(updatedTemplate);
    }

    // 6. Lấy tất cả form templates với phân trang và tìm kiếm
    public ResultPaginationDTO getAllFormTemplatesWithPaginationAndSearch(
            @NonNull Specification<FormTemplate> specification,
            @NonNull Pageable pageable) {
        Page<FormTemplate> templates = formTemplateRepository.findAll(specification, pageable);
        return Utils.toResultPaginationDTO(templates.map(formTemplateMapper::toFormTemplateResponse), pageable);
    }

}
