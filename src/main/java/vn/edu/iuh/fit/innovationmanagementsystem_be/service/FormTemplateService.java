package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateMultipleFormTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateMultipleFormTemplatesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
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
    private final FormTemplateMapper formTemplateMapper;

    public FormTemplateService(FormTemplateRepository formTemplateRepository,
            InnovationRoundRepository innovationRoundRepository,
            FormTemplateMapper formTemplateMapper) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationRoundRepository = innovationRoundRepository;
        this.formTemplateMapper = formTemplateMapper;
    }

    // 1. Get form template by id
    public FormTemplateResponse getFormTemplateById(String id) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        return formTemplateMapper.toFormTemplateResponse(template);
    }

    // 2. Get all form templates by innovation round
    public List<FormTemplateResponse> getFormTemplatesByInnovationRound(String roundId) {
        List<FormTemplate> templates = formTemplateRepository.findByInnovationRoundIdOrderByName(roundId);
        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
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
        return formTemplateMapper.toFormTemplateResponse(savedTemplate);
    }

    // 4. Create Multiple Form Templates
    @Transactional
    public CreateMultipleFormTemplatesResponse createMultipleFormTemplates(CreateMultipleFormTemplatesRequest request) {

        InnovationRound innovationRound = innovationRoundRepository.findById(request.getInnovationRoundId())
                .orElseThrow(() -> new IdInvalidException(
                        "Innovation round không tồn tại với ID: " + request.getInnovationRoundId()));

        if (request.getFormTemplates() == null || request.getFormTemplates().isEmpty()) {
            throw new IdInvalidException("Danh sách form templates không được để trống");
        }

        List<String> templateNames = request.getFormTemplates().stream()
                .map(CreateMultipleFormTemplatesRequest.FormTemplateData::getName)
                .collect(Collectors.toList());

        if (templateNames.size() != templateNames.stream().distinct().count()) {
            throw new IdInvalidException("Danh sách form templates có tên trùng lặp");
        }

        List<FormTemplate> formTemplates = request.getFormTemplates().stream()
                .map(templateData -> {
                    FormTemplate template = new FormTemplate();
                    template.setName(templateData.getName());
                    template.setDescription(templateData.getDescription());
                    template.setInnovationRound(innovationRound);
                    return template;
                })
                .collect(Collectors.toList());

        List<FormTemplate> savedFormTemplates = formTemplateRepository.saveAll(formTemplates);

        List<FormTemplateResponse> formTemplateResponses = savedFormTemplates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());

        return new CreateMultipleFormTemplatesResponse(
                request.getInnovationRoundId(),
                innovationRound.getName(),
                formTemplateResponses);
    }

    // 5. Update form template
    public FormTemplateResponse updateFormTemplate(String id, UpdateFormTemplateRequest request) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        template.setName(request.getName());
        template.setDescription(request.getDescription());

        FormTemplate updatedTemplate = formTemplateRepository.save(template);
        return formTemplateMapper.toFormTemplateResponse(updatedTemplate);
    }

    // 6. Get all form templates with pagination and search
    public ResultPaginationDTO getAllFormTemplatesWithPaginationAndSearch(
            @NonNull Specification<FormTemplate> specification,
            @NonNull Pageable pageable) {
        Page<FormTemplate> templates = formTemplateRepository.findAll(specification, pageable);
        return Utils.toResultPaginationDTO(templates.map(formTemplateMapper::toFormTemplateResponse), pageable);
    }

}
