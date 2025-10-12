package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateMultipleFormTemplatesRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateWithFieldsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateMultipleFormTemplatesResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateWithFieldsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationPhaseRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
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
    private final InnovationRoundRepository innovationRoundRepository;

    public FormTemplateService(FormTemplateRepository formTemplateRepository,
            InnovationPhaseRepository innovationPhaseRepository,
            InnovationRoundRepository innovationRoundRepository,
            FormTemplateMapper formTemplateMapper) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationPhaseRepository = innovationPhaseRepository;
        this.innovationRoundRepository = innovationRoundRepository;
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

    // 6. Tạo form template với fields
    @Transactional
    public CreateTemplateWithFieldsResponse createTemplateWithFields(CreateTemplateWithFieldsRequest request) {
        // Tìm innovation round
        InnovationRound innovationRound = innovationRoundRepository.findById(request.getRoundId().trim())
                .orElseThrow(
                        () -> new IdInvalidException("Innovation round không tồn tại với ID: " + request.getRoundId()));

        // Tạo form template
        FormTemplate template = new FormTemplate();
        template.setTemplateType(request.getTemplateType());
        template.setTargetRole(request.getTargetRole());
        template.setTemplateContent(request.getTemplateContent());
        template.setInnovationRound(innovationRound);

        FormTemplate savedTemplate = formTemplateRepository.save(template);

        // Tạo form fields
        List<FormField> formFields = request.getFields().stream()
                .map(fieldData -> createFormField(fieldData, savedTemplate))
                .collect(Collectors.toList());

        savedTemplate.setFormFields(formFields);
        formTemplateRepository.save(savedTemplate);

        return createTemplateWithFieldsResponse(savedTemplate);
    }

    private FormField createFormField(CreateTemplateWithFieldsRequest.FieldData fieldData, FormTemplate template) {
        FormField field = new FormField();
        field.setFieldKey(fieldData.getFieldKey());
        field.setLabel(fieldData.getLabel());
        field.setFieldType(fieldData.getType());
        field.setRequired(fieldData.getRequired());
        field.setPlaceholder(fieldData.getPlaceholder());
        field.setFormTemplate(template);

        // Xử lý table config nếu field type là TABLE
        if (fieldData.getType() == FieldTypeEnum.TABLE && fieldData.getTableConfig() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode tableConfigJson = mapper.valueToTree(fieldData.getTableConfig());
                field.setTableConfig(tableConfigJson);
            } catch (Exception e) {
                throw new IdInvalidException("Lỗi khi xử lý table config: " + e.getMessage());
            }
        }

        // Xử lý options nếu field có options (DROPDOWN, RADIO, CHECKBOX)
        if (fieldData.getOptions() != null && !fieldData.getOptions().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode optionsJson = mapper.valueToTree(fieldData.getOptions());
                field.setOptions(optionsJson);
            } catch (Exception e) {
                throw new IdInvalidException("Lỗi khi xử lý options: " + e.getMessage());
            }
        }

        return field;
    }

    private CreateTemplateWithFieldsResponse createTemplateWithFieldsResponse(FormTemplate template) {
        CreateTemplateWithFieldsResponse response = new CreateTemplateWithFieldsResponse();
        response.setId(template.getId());
        response.setTemplateContent(template.getTemplateContent());
        response.setTemplateType(template.getTemplateType());
        response.setTargetRole(template.getTargetRole());
        response.setRoundId(template.getInnovationRound().getId());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        response.setCreatedBy(template.getCreatedBy());
        response.setUpdatedBy(template.getUpdatedBy());

        List<CreateTemplateWithFieldsResponse.FieldResponse> fieldResponses = template.getFormFields().stream()
                .map(this::convertToFieldResponse)
                .collect(Collectors.toList());

        response.setFields(fieldResponses);
        return response;
    }

    private CreateTemplateWithFieldsResponse.FieldResponse convertToFieldResponse(FormField field) {
        CreateTemplateWithFieldsResponse.FieldResponse fieldResponse = new CreateTemplateWithFieldsResponse.FieldResponse();
        fieldResponse.setId(field.getId());
        fieldResponse.setFieldKey(field.getFieldKey());
        fieldResponse.setLabel(field.getLabel());
        fieldResponse.setType(field.getFieldType());
        fieldResponse.setRequired(field.getRequired());
        fieldResponse.setPlaceholder(field.getPlaceholder());
        fieldResponse.setTableConfig(field.getTableConfig());
        fieldResponse.setOptions(field.getOptions());
        return fieldResponse;
    }

    // 7. Lấy tất cả form templates với phân trang và tìm kiếm
    public ResultPaginationDTO getAllFormTemplatesWithPaginationAndSearch(
            @NonNull Specification<FormTemplate> specification,
            @NonNull Pageable pageable) {
        Page<FormTemplate> templates = formTemplateRepository.findAll(specification, pageable);
        return Utils.toResultPaginationDTO(templates.map(formTemplateMapper::toFormTemplateResponse), pageable);
    }

}
