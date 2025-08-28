package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final InnovationRoundRepository innovationRoundRepository;

    public FormTemplateService(FormTemplateRepository formTemplateRepository,
            InnovationRoundRepository innovationRoundRepository) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationRoundRepository = innovationRoundRepository;
    }

    // 1. Get form template by id
    public FormTemplateResponse getFormTemplateById(String id) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        return mapToResponse(template);
    }

    // 2. Get all form templates by innovation round
    public List<FormTemplateResponse> getFormTemplatesByInnovationRound(String roundId) {
        List<FormTemplate> templates = formTemplateRepository.findByInnovationRoundIdOrderByName(roundId);
        return templates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. Create form template
    public FormTemplateResponse createFormTemplate(CreateFormTemplateRequest request) {
        InnovationRound round = innovationRoundRepository.findById(request.getInnovationRoundId())
                .orElseThrow(() -> new IdInvalidException(
                        "Innovation round không tồn tại với ID: " + request.getInnovationRoundId()));

        FormTemplate template = new FormTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setInnovationRound(round);

        FormTemplate savedTemplate = formTemplateRepository.save(template);
        return mapToResponse(savedTemplate);
    }

    // 4. Update form template
    public FormTemplateResponse updateFormTemplate(String id, UpdateFormTemplateRequest request) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        template.setName(request.getName());
        template.setDescription(request.getDescription());

        FormTemplate updatedTemplate = formTemplateRepository.save(template);
        return mapToResponse(updatedTemplate);
    }

    // 5. Delete form template
    public void deleteFormTemplate(String id) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        // Kiểm tra template có được sử dụng bởi innovation nào không
        if (formTemplateRepository.isTemplateUsedByInnovation(id)) {
            throw new RuntimeException("Không thể xóa template đã được sử dụng bởi innovation");
        }

        formTemplateRepository.delete(template);
    }

    // 6. Get all form templates with pagination and search
    public ResultPaginationDTO getAllFormTemplatesWithPaginationAndSearch(
            @NonNull Specification<FormTemplate> specification,
            @NonNull Pageable pageable) {
        Page<FormTemplate> templates = formTemplateRepository.findAll(specification, pageable);
        return Utils.toResultPaginationDTO(templates.map(this::mapToResponse), pageable);
    }

    // Map entity sang response DTO
    private FormTemplateResponse mapToResponse(FormTemplate template) {
        FormTemplateResponse response = new FormTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setInnovationRoundId(template.getInnovationRound().getId());
        response.setInnovationRoundName(template.getInnovationRound().getName());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        response.setCreatedBy(template.getCreatedBy());
        response.setUpdatedBy(template.getUpdatedBy());

        // Map form fields
        if (template.getFormFields() != null) {
            response.setFormFields(template.getFormFields().stream()
                    .map(this::toFormFieldResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    // Mapper
    private FormFieldResponse toFormFieldResponse(FormField field) {
        FormFieldResponse response = new FormFieldResponse();
        response.setId(field.getId());
        response.setLabel(field.getLabel());
        response.setFieldType(field.getFieldType());
        response.setIsRequired(field.getIsRequired());
        response.setOrderInTemplate(field.getOrderInTemplate());
        return response;
    }
}
