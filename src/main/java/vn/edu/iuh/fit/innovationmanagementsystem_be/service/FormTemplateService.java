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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateWithFieldsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
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
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
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

    // 2. Lấy tất cả form templates by innovation phase
    public List<FormTemplateResponse> getFormTemplatesByInnovationPhase(String phaseId) {

        InnovationPhase phase = innovationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IdInvalidException("Innovation phase không tồn tại với ID: " + phaseId));

        List<FormTemplate> templates = formTemplateRepository
                .findByInnovationRoundIdOrderByTemplateType(phase.getInnovationRound().getId());
        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 3. Lấy tất cả form templates by innovation round
    public List<FormTemplateResponse> getFormTemplatesByInnovationRound(String roundId) {
        innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Innovation round không tồn tại với ID: " + roundId));

        List<FormTemplate> templates = formTemplateRepository
                .findByInnovationRoundIdOrderByTemplateType(roundId);
        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 4. Lấy tất cả form templates theo innovation round hiện tại
    public List<FormTemplateResponse> getFormTemplatesByCurrentRound() {
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(LocalDate.now())
                .orElseThrow(() -> new IdInvalidException("Không có innovation round hiện tại"));

        List<FormTemplate> templates = formTemplateRepository
                .findByInnovationRoundIdOrderByTemplateType(currentRound.getId());
        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 5. Cập nhật form template
    public FormTemplateResponse updateFormTemplate(String id, UpdateFormTemplateRequest request) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        InnovationRound round = template.getInnovationRound();
        if (round == null || round.getStatus() != InnovationRoundStatusEnum.DRAFT) {
            throw new IdInvalidException("Chỉ được cập nhật form template khi vòng đang ở trạng thái DRAFT");
        }

        if (request.getTemplateType() == null && request.getTargetRole() == null
                && request.getTemplateContent() == null && (request.getFields() == null)) {
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

        // Upsert danh sách FormField nếu có
        if (request.getFields() != null) {
            // Map các field hiện có theo id để tiện cập nhật/xóa
            Map<String, FormField> existingById = template.getFormFields().stream()
                    .filter(f -> f.getId() != null)
                    .collect(java.util.stream.Collectors.toMap(FormField::getId, f -> f));

            Set<String> incomingIds = new java.util.HashSet<>();

            List<FormField> newList = new java.util.ArrayList<>();
            for (UpdateFormTemplateRequest.FieldData fd : request.getFields()) {
                FormField entity = null;
                if (fd.getId() != null && existingById.containsKey(fd.getId())) {
                    entity = existingById.get(fd.getId());
                    incomingIds.add(fd.getId());
                } else {
                    entity = new FormField();
                    entity.setFormTemplate(template);
                }

                if (fd.getFieldKey() != null)
                    entity.setFieldKey(fd.getFieldKey());
                if (fd.getLabel() != null)
                    entity.setLabel(fd.getLabel());
                if (fd.getType() != null)
                    entity.setFieldType(fd.getType());
                if (fd.getRequired() != null)
                    entity.setRequired(fd.getRequired());
                if (fd.getPlaceholder() != null)
                    entity.setPlaceholder(fd.getPlaceholder());
                if (fd.getRepeatable() != null)
                    entity.setRepeatable(fd.getRepeatable());

                // tableConfig/options/children: tận dụng mapper như create
                if (fd.getType() == FieldTypeEnum.TABLE && fd.getTableConfig() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode tableConfigJson = mapper
                                .valueToTree(fd.getTableConfig());
                        entity.setTableConfig(tableConfigJson);
                    } catch (Exception e) {
                        throw new IdInvalidException("Lỗi khi xử lý table config: " + e.getMessage());
                    }
                } else if (fd.getType() != FieldTypeEnum.TABLE) {
                    entity.setTableConfig(null);
                }

                if (fd.getOptions() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode optionsJson = mapper.valueToTree(fd.getOptions());
                        entity.setOptions(optionsJson);
                    } catch (Exception e) {
                        throw new IdInvalidException("Lỗi khi xử lý options: " + e.getMessage());
                    }
                }

                if (fd.getChildren() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode childrenJson = mapper.valueToTree(fd.getChildren());
                        entity.setChildren(childrenJson);
                    } catch (Exception e) {
                        throw new IdInvalidException("Lỗi khi xử lý children: " + e.getMessage());
                    }
                }

                newList.add(entity);
            }

            // Xóa các field không còn trong request (orphanRemoval - db)
            template.getFormFields().clear();
            template.getFormFields().addAll(newList);
        }

        FormTemplate updatedTemplate = formTemplateRepository.save(template);
        return formTemplateMapper.toFormTemplateResponse(updatedTemplate);
    }

    // 6. Tạo form template với fields
    @Transactional
    public CreateTemplateWithFieldsResponse createTemplateWithFields(CreateTemplateWithFieldsRequest request) {
        InnovationRound innovationRound = innovationRoundRepository.findById(request.getRoundId().trim())
                .orElseThrow(
                        () -> new IdInvalidException("Innovation round không tồn tại với ID: " + request.getRoundId()));

        FormTemplate template = new FormTemplate();
        template.setTemplateType(request.getTemplateType());
        template.setTargetRole(request.getTargetRole());
        template.setTemplateContent(request.getTemplateContent());
        template.setInnovationRound(innovationRound);

        FormTemplate savedTemplate = formTemplateRepository.save(template);

        List<FormField> formFields = request.getFields().stream()
                .map(fieldData -> createFormField(fieldData, savedTemplate))
                .collect(Collectors.toList());

        savedTemplate.setFormFields(formFields);
        formTemplateRepository.save(savedTemplate);

        return createTemplateWithFieldsResponse(savedTemplate);
    }

    // 7. Lấy tất cả form templates với phân trang và tìm kiếm
    public ResultPaginationDTO getAllFormTemplatesWithPaginationAndSearch(
            @NonNull Specification<FormTemplate> specification,
            @NonNull Pageable pageable) {
        Page<FormTemplate> templates = formTemplateRepository.findAll(specification, pageable);
        return Utils.toResultPaginationDTO(templates.map(formTemplateMapper::toFormTemplateResponse), pageable);
    }

    private FormField createFormField(CreateTemplateWithFieldsRequest.FieldData fieldData, FormTemplate template) {
        FormField field = new FormField();
        field.setFieldKey(fieldData.getFieldKey());
        field.setLabel(fieldData.getLabel());
        field.setFieldType(fieldData.getType());
        field.setRequired(fieldData.getRequired());
        field.setPlaceholder(fieldData.getPlaceholder());
        field.setFormTemplate(template);
        field.setRepeatable(fieldData.getRepeatable() != null ? fieldData.getRepeatable() : false);

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

        // Xử lý children nếu field có children (SECTION type)
        if (fieldData.getChildren() != null && !fieldData.getChildren().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode childrenJson = mapper.valueToTree(fieldData.getChildren());
                field.setChildren(childrenJson);
            } catch (Exception e) {
                throw new IdInvalidException("Lỗi khi xử lý children: " + e.getMessage());
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
        fieldResponse.setRepeatable(field.getRepeatable());
        fieldResponse.setChildren(convertChildrenToFieldResponse(field.getChildren()));
        return fieldResponse;
    }

    private List<CreateTemplateWithFieldsResponse.FieldResponse> convertChildrenToFieldResponse(JsonNode childrenJson) {
        if (childrenJson == null || childrenJson.isNull()) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<CreateTemplateWithFieldsRequest.FieldData> childrenData = mapper.treeToValue(childrenJson,
                    mapper.getTypeFactory().constructCollectionType(List.class,
                            CreateTemplateWithFieldsRequest.FieldData.class));

            return childrenData.stream()
                    .map(childData -> {
                        CreateTemplateWithFieldsResponse.FieldResponse childResponse = new CreateTemplateWithFieldsResponse.FieldResponse();
                        childResponse.setId(childData.getId());
                        childResponse.setFieldKey(childData.getFieldKey());
                        childResponse.setLabel(childData.getLabel());
                        childResponse.setType(childData.getType());
                        childResponse.setRequired(childData.getRequired());
                        childResponse.setPlaceholder(childData.getPlaceholder());
                        childResponse.setRepeatable(childData.getRepeatable());
                        childResponse.setOptions(
                                childData.getOptions() != null ? mapper.valueToTree(childData.getOptions()) : null);
                        childResponse.setChildren(convertChildrenToFieldResponse(
                                childData.getChildren() != null ? mapper.valueToTree(childData.getChildren()) : null));
                        return childResponse;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

}
