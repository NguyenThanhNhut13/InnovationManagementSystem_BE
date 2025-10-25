package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateWithFieldsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FormFieldRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateWithFieldsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormFieldResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.HtmlTemplateUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@Transactional
@Slf4j
public class FormTemplateService {

    @PersistenceContext
    private EntityManager entityManager;

    private final FormTemplateRepository formTemplateRepository;
    private final FormTemplateMapper formTemplateMapper;
    private final InnovationRoundRepository innovationRoundRepository;
    private final HtmlTemplateUtils htmlTemplateUtils;
    private final UserService userService;

    public FormTemplateService(FormTemplateRepository formTemplateRepository,
            InnovationRoundRepository innovationRoundRepository,
            FormTemplateMapper formTemplateMapper,
            HtmlTemplateUtils htmlTemplateUtils,
            UserService userService) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationRoundRepository = innovationRoundRepository;
        this.formTemplateMapper = formTemplateMapper;
        this.htmlTemplateUtils = htmlTemplateUtils;
        this.userService = userService;
    }

    // 1. Lấy form template by id
    public CreateTemplateWithFieldsResponse getFormTemplateById(String id) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        return formTemplateMapper.toCreateTemplateWithFieldsResponse(template);
    }

    // 2. Lấy tất cả form templates theo innovation round hiện tại
    public List<FormTemplateResponse> getFormTemplatesByCurrentRound() {
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(LocalDate.now())
                .orElse(null);

        List<FormTemplate> templates;

        if (currentRound != null) {
            // Có current round - lấy templates của round đó
            templates = formTemplateRepository
                    .findByInnovationRoundIdOrderByTemplateType(currentRound.getId());
        } else {
            // Không có current round - lấy templates từ template library
            // (innovation_round_id = null)
            templates = formTemplateRepository
                    .findByInnovationRoundIsNullOrderByTemplateType();
        }

        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 3. Cập nhật form template
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
            String encodedContent = Utils.encode(request.getTemplateContent());
            template.setTemplateContent(encodedContent);
        }

        // Upsert danh sách FormField nếu có
        if (request.getFields() != null) {
            processFormFields(template, request.getFields());
        }

        FormTemplate updatedTemplate = formTemplateRepository.save(template);

        // Cập nhật HTML template content với field IDs mới sau khi fields đã được save
        updateHtmlTemplateContent(updatedTemplate);
        updatedTemplate = formTemplateRepository.save(updatedTemplate);

        return formTemplateMapper.toFormTemplateResponse(updatedTemplate);
    }

    // 4. Tạo form template với fields
    @Transactional
    public CreateTemplateWithFieldsResponse createTemplateWithFields(CreateTemplateWithFieldsRequest request) {
        InnovationRound innovationRound = innovationRoundRepository.findById(request.getRoundId().trim())
                .orElseThrow(
                        () -> new IdInvalidException("Innovation round không tồn tại với ID: " + request.getRoundId()));

        FormTemplate template = new FormTemplate();
        template.setTemplateType(request.getTemplateType());
        template.setTargetRole(request.getTargetRole());

        String encodedContent = Utils.encode(request.getTemplateContent());
        template.setTemplateContent(encodedContent);
        template.setInnovationRound(innovationRound);

        FormTemplate savedTemplate = formTemplateRepository.save(template);

        // Sử dụng cùng logic xử lý fields như updateFormTemplate
        if (request.getFields() != null && !request.getFields().isEmpty()) {
            processFormFields(savedTemplate, request.getFields());
        }

        FormTemplate finalTemplate = formTemplateRepository.save(savedTemplate);

        // Cập nhật HTML template content với field IDs mới sau khi fields đã được save
        updateHtmlTemplateContent(finalTemplate);
        finalTemplate = formTemplateRepository.save(finalTemplate);

        return formTemplateMapper.toCreateTemplateWithFieldsResponse(finalTemplate);
    }

    // 5. Lấy tất cả form templates với phân trang và tìm kiếm
    public ResultPaginationDTO getAllFormTemplatesWithPaginationAndSearch(
            @NonNull Specification<FormTemplate> specification,
            @NonNull Pageable pageable) {
        Page<FormTemplate> templates = formTemplateRepository.findAll(specification, pageable);
        return Utils.toResultPaginationDTO(templates.map(formTemplateMapper::toFormTemplateResponse), pageable);
    }

    // 6. Xóa form template (chỉ khi round đang DRAFT)
    public void deleteFormTemplate(String id) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        InnovationRound round = template.getInnovationRound();
        if (round == null || round.getStatus() != InnovationRoundStatusEnum.DRAFT) {
            throw new IdInvalidException("Chỉ được xóa form template khi vòng đang ở trạng thái DRAFT");
        }

        formTemplateRepository.delete(template);
    }

    // 7. Lấy FormTemplate (không gắn round cụ thể) với pagination và filtering
    public ResultPaginationDTO getTemplateLibraryWithPaginationAndSearch(
            @NonNull Specification<FormTemplate> specification,
            @NonNull Pageable pageable) {

        Specification<FormTemplate> librarySpecification = specification
                .and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("innovationRound")));

        Page<FormTemplate> templates = formTemplateRepository.findAll(librarySpecification, pageable);
        return Utils.toResultPaginationDTO(templates.map(formTemplateMapper::toFormTemplateResponse), pageable);
    }

    private void processFormFields(FormTemplate template, List<FormFieldRequest> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        Map<String, FormField> existingById = template.getFormFields().stream()
                .filter(f -> f.getId() != null)
                .collect(java.util.stream.Collectors.toMap(FormField::getId, f -> f));

        Set<String> incomingIds = new java.util.HashSet<>();

        List<FormField> newList = new java.util.ArrayList<>();
        for (FormFieldRequest fd : fields) {
            FormField entity = null;

            // Ưu tiên tìm theo ID trước
            if (fd.getId() != null && existingById.containsKey(fd.getId())) {
                entity = existingById.get(fd.getId());
                incomingIds.add(fd.getId());
            } else {
                // Nếu không có ID, tạo field mới
                entity = new FormField();
                entity.setFormTemplate(template);
            }

            // Cập nhật tất cả các field từ request
            if (fd.getFieldKey() != null)
                entity.setFieldKey(fd.getFieldKey());
            if (fd.getLabel() != null)
                entity.setLabel(fd.getLabel());
            if (fd.getType() != null)
                entity.setFieldType(fd.getType());
            if (fd.getRequired() != null)
                entity.setRequired(fd.getRequired());
            if (fd.getIsReadOnly() != null)
                entity.setIsReadOnly(fd.getIsReadOnly());
            if (fd.getRepeatable() != null)
                entity.setRepeatable(fd.getRepeatable());

            // tableConfig/options/children: tận dụng mapper như create
            if (fd.getType() == FieldTypeEnum.TABLE && fd.getTableConfig() != null) {
                // Tự sinh UUID cho các column nếu chưa có
                JsonNode processedTableConfig = generateColumnIdsIfNeeded(fd.getTableConfig());
                entity.setTableConfig(processedTableConfig);
            } else if (fd.getType() != FieldTypeEnum.TABLE) {
                entity.setTableConfig(null);
            }

            if (fd.getOptions() != null) {
                entity.setOptions(fd.getOptions());
            }

            if (fd.getChildren() != null) {
                JsonNode processedChildren = generateChildrenIdsIfNeeded(fd.getChildren());
                entity.setChildren(processedChildren);
            }

            if (fd.getReferenceConfig() != null) {
                entity.setReferenceConfig(fd.getReferenceConfig());
            }

            if (fd.getUserDataConfig() != null) {
                entity.setUserDataConfig(fd.getUserDataConfig());
            }

            if (fd.getInnovationDataConfig() != null) {
                entity.setInnovationDataConfig(fd.getInnovationDataConfig());
            }

            if (fd.getContributionConfig() != null) {
                entity.setContributionConfig(fd.getContributionConfig());
            }

            if (fd.getSigningRole() != null) {
                entity.setSigningRole(fd.getSigningRole());
            }

            newList.add(entity);
        }

        // Xóa các field không còn được sử dụng (chỉ dựa vào ID)
        List<FormField> fieldsToRemove = template.getFormFields().stream()
                .filter(field -> {
                    // Giữ lại field nếu có ID trong incomingIds
                    if (field.getId() != null && incomingIds.contains(field.getId())) {
                        return false;
                    }
                    // Xóa field nếu không có ID hoặc ID không có trong incomingIds
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        template.getFormFields().removeAll(fieldsToRemove);
        template.getFormFields().addAll(newList);
    }

    private void updateHtmlTemplateContent(FormTemplate template) {
        if (template.getTemplateContent() == null || template.getFormFields().isEmpty()) {
            return;
        }

        try {
            String htmlContent = Utils.decode(template.getTemplateContent());
            String updatedHtmlContent = htmlTemplateUtils.updateFieldIdsInHtml(htmlContent, template.getFormFields());
            String encodedContent = Utils.encode(updatedHtmlContent);
            template.setTemplateContent(encodedContent);

        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật HTML template content: " + e.getMessage());
        }
    }

    @Transactional
    public CreateTemplateResponse createTemplate(CreateTemplateRequest request) {
        try {

            FormTemplate template = new FormTemplate();
            template.setTemplateType(request.getTemplateType());
            template.setTargetRole(request.getTargetRole());

            String encodedContent = Utils.encode(request.getTemplateContent());
            template.setTemplateContent(encodedContent);

            if (request.getRoundId() != null && !request.getRoundId().trim().isEmpty()) {
                InnovationRound innovationRound = innovationRoundRepository.findById(request.getRoundId().trim())
                        .orElseThrow(() -> new IdInvalidException(
                                "Innovation round không tồn tại với ID: " + request.getRoundId()));
                template.setInnovationRound(innovationRound);
            } else {
                template.setInnovationRound(null);
            }

            FormTemplate savedTemplate = formTemplateRepository.save(template);
            entityManager.flush();

            if (request.getFields() != null && !request.getFields().isEmpty()) {
                processFormFields(savedTemplate, request.getFields());
            }

            FormTemplate finalTemplate = formTemplateRepository.save(savedTemplate);
            entityManager.flush();

            updateHtmlTemplateContent(finalTemplate);
            finalTemplate = formTemplateRepository.save(finalTemplate);
            entityManager.flush();

            return formTemplateMapper.toCreateTemplateResponse(finalTemplate);
        } catch (Exception e) {
            log.error("Lỗi khi tạo form template: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 9. Lấy form templates theo innovation round ID
    public List<FormTemplateResponse> getFormTemplatesByInnovationRound(String roundId) {
        innovationRoundRepository.findById(roundId)
                .orElseThrow(() -> new IdInvalidException("Innovation round không tồn tại với ID: " + roundId));

        List<FormTemplate> templates = formTemplateRepository
                .findByInnovationRoundIdOrderByTemplateType(roundId);
        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 10. Lấy form templates theo innovation round hiện tại và target role
    public List<FormTemplateResponse> getFormTemplatesByCurrentRoundAndTargetRole(String targetRole) {
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(LocalDate.now())
                .orElse(null);

        log.info("Current round: {}", currentRound != null ? currentRound.getId() : "null");

        // Convert String to TargetRoleCode enum
        TargetRoleCode targetRoleEnum = TargetRoleCode.valueOf(targetRole);
        log.info("Target role enum: {}", targetRoleEnum);

        List<FormTemplate> templates;

        if (currentRound != null) {
            // Có current round - lấy templates của round đó
            templates = formTemplateRepository
                    .findByInnovationRoundIdAndTargetRoleOrderByTemplateType(currentRound.getId(), targetRoleEnum);
            log.info("Found {} templates in current round {} for role {}", templates.size(), currentRound.getId(),
                    targetRoleEnum);
        } else {
            // Không có current round - lấy templates từ template library
            // (innovation_round_id = null)
            templates = formTemplateRepository
                    .findByInnovationRoundIsNullAndTargetRoleOrderByTemplateType(targetRoleEnum);
            log.info("Found {} templates in template library for role {}", templates.size(), targetRoleEnum);
        }

        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    /**
     * Loại bỏ các field trùng lặp theo fieldKey trong danh sách templates
     */
    private List<FormTemplateResponse> removeDuplicateFields(List<FormTemplateResponse> templates) {
        return templates.stream()
                .map(template -> {
                    // Tạo map để lưu trữ field theo fieldKey, giữ lại field đầu tiên
                    Map<String, FormFieldResponse> fieldMap = new LinkedHashMap<>();

                    for (FormFieldResponse field : template.getFormFields()) {
                        String fieldKey = field.getFieldKey();
                        if (!fieldMap.containsKey(fieldKey)) {
                            fieldMap.put(fieldKey, field);
                        } else {
                            log.debug("Removing duplicate field with key: {} from template: {}",
                                    fieldKey, template.getId());
                        }
                    }

                    // Tạo template mới với danh sách field đã loại bỏ trùng lặp
                    FormTemplateResponse newTemplate = new FormTemplateResponse();
                    newTemplate.setId(template.getId());
                    newTemplate.setTemplateType(template.getTemplateType());
                    newTemplate.setTargetRole(template.getTargetRole());
                    newTemplate.setTemplateContent(template.getTemplateContent());
                    newTemplate.setInnovationRoundId(template.getInnovationRoundId());
                    newTemplate.setInnovationRoundName(template.getInnovationRoundName());
                    newTemplate.setFormFields(new ArrayList<>(fieldMap.values()));
                    newTemplate.setCreatedAt(template.getCreatedAt());
                    newTemplate.setUpdatedAt(template.getUpdatedAt());
                    newTemplate.setCreatedBy(template.getCreatedBy());
                    newTemplate.setUpdatedBy(template.getUpdatedBy());

                    return newTemplate;
                })
                .collect(Collectors.toList());
    }

    // 11. Lấy form templates theo roles của user hiện tại
    public List<FormTemplateResponse> getFormTemplatesByCurrentUserRoles() {
        UserResponse currentUser = userService.getCurrentUserResponse();
        List<String> userRoles = currentUser.getRoles();

        log.info("Current user: {}", currentUser.getEmail());
        log.info("User roles: {}", userRoles);

        String targetRole = determineTargetRoleFromUserRoles(userRoles);
        log.info("Determined target role: {}", targetRole);

        List<FormTemplateResponse> templates = getFormTemplatesByCurrentRoundAndTargetRole(targetRole);
        log.info("Found {} templates for target role: {}", templates.size(), targetRole);

        // Debug: Kiểm tra tất cả templates trong database
        List<FormTemplate> allTemplates = formTemplateRepository.findAll();
        log.info("Total templates in database: {}", allTemplates.size());

        List<FormTemplate> libraryTemplates = formTemplateRepository.findByInnovationRoundIsNullOrderByTemplateType();
        log.info("Templates in library: {}", libraryTemplates.size());

        for (FormTemplate template : libraryTemplates) {
            log.info("Library template: {} - {} - {}", template.getId(), template.getTemplateType(),
                    template.getTargetRole());
        }

        // Thêm logic quét CONTRIBUTED fields từ templates 3,4,5
        return enhanceTemplatesWithContributedFields(templates);
    }

    private String determineTargetRoleFromUserRoles(List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return "EMPLOYEE";
        }

        // Kiểm tra theo thứ tự ưu tiên
        for (String role : userRoles) {
            switch (role) {
                case "QUAN_TRI_VIEN_QLKH_HTQT":
                    return "QLKH_SECRETARY";
                case "TRUONG_KHOA":
                    return "DEPARTMENT";
                case "GIANG_VIEN":
                    return "EMPLOYEE";
            }
        }
        return "EMPLOYEE";
    }

    /**
     * Tăng cường templates với CONTRIBUTED fields từ templates 3,4,5
     */
    private List<FormTemplateResponse> enhanceTemplatesWithContributedFields(List<FormTemplateResponse> templates) {
        // Lấy danh sách template IDs từ templates 1,2 (DON_DE_NGHI, BAO_CAO_MO_TA)
        List<String> targetTemplateIds = templates.stream()
                .filter(template -> template.getTemplateType() == TemplateTypeEnum.DON_DE_NGHI ||
                        template.getTemplateType() == TemplateTypeEnum.BAO_CAO_MO_TA)
                .map(FormTemplateResponse::getId)
                .collect(Collectors.toList());

        if (targetTemplateIds.isEmpty()) {
            return templates;
        }

        // Lấy CONTRIBUTED fields từ templates 3,4,5
        List<FormField> contributedFields = getContributedFieldsFromTemplates345(targetTemplateIds);

        // Thêm contributed fields vào templates tương ứng
        List<FormTemplateResponse> enhancedTemplates = templates.stream()
                .map(template -> enhanceTemplateWithContributedFields(template, contributedFields))
                .collect(Collectors.toList());

        // Loại bỏ các field trùng lặp theo fieldKey
        return removeDuplicateFields(enhancedTemplates);
    }

    /**
     * Lấy CONTRIBUTED fields từ templates 3,4,5 có targetTemplateIds khớp
     */
    private List<FormField> getContributedFieldsFromTemplates345(List<String> targetTemplateIds) {
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(LocalDate.now())
                .orElse(null);

        List<FormTemplate> templates345;
        if (currentRound != null) {
            templates345 = formTemplateRepository.findByInnovationRoundIdAndTemplateTypeIn(
                    currentRound.getId(),
                    List.of(TemplateTypeEnum.BIEN_BAN_HOP, TemplateTypeEnum.TONG_HOP_DE_NGHI,
                            TemplateTypeEnum.TONG_HOP_CHAM_DIEM));
        } else {
            templates345 = formTemplateRepository.findByInnovationRoundIsNullAndTemplateTypeIn(
                    List.of(TemplateTypeEnum.BIEN_BAN_HOP, TemplateTypeEnum.TONG_HOP_DE_NGHI,
                            TemplateTypeEnum.TONG_HOP_CHAM_DIEM));
        }

        return templates345.stream()
                .flatMap(template -> extractContributedFields(template, targetTemplateIds).stream())
                .collect(Collectors.toList());
    }

    /**
     * Trích xuất CONTRIBUTED fields từ một template
     */
    private List<FormField> extractContributedFields(FormTemplate template, List<String> targetTemplateIds) {
        List<FormField> contributedFields = new ArrayList<>();

        for (FormField field : template.getFormFields()) {
            if (field.getFieldType() == FieldTypeEnum.CONTRIBUTED) {
                if (field.getContributionConfig() != null) {
                    // Kiểm tra targetTemplateIds trong contributionConfig JSON
                    JsonNode targetTemplateIdsNode = field.getContributionConfig().get("targetTemplateIds");
                    if (targetTemplateIdsNode != null && targetTemplateIdsNode.isArray()) {
                        boolean hasMatchingTarget = false;
                        for (JsonNode targetId : targetTemplateIdsNode) {
                            if (targetTemplateIds.contains(targetId.asText())) {
                                hasMatchingTarget = true;
                                break;
                            }
                        }

                        if (hasMatchingTarget) {
                            contributedFields.add(field);
                        }
                    }
                }
            } else if (field.getFieldType() == FieldTypeEnum.TABLE && field.getTableConfig() != null) {
                // Xử lý TABLE fields - kiểm tra columns
                JsonNode columnsNode = field.getTableConfig().get("columns");
                if (columnsNode != null && columnsNode.isArray()) {
                    for (JsonNode columnNode : columnsNode) {
                        JsonNode columnTypeNode = columnNode.get("type");
                        if (columnTypeNode != null && "CONTRIBUTED".equals(columnTypeNode.asText())) {
                            JsonNode columnContributionConfig = columnNode.get("contributionConfig");
                            if (columnContributionConfig != null) {
                                JsonNode columnTargetTemplateIds = columnContributionConfig.get("targetTemplateIds");
                                if (columnTargetTemplateIds != null && columnTargetTemplateIds.isArray()) {
                                    boolean hasMatchingTarget = false;
                                    for (JsonNode targetId : columnTargetTemplateIds) {
                                        if (targetTemplateIds.contains(targetId.asText())) {
                                            hasMatchingTarget = true;
                                            break;
                                        }
                                    }

                                    if (hasMatchingTarget) {
                                        // Tạo FormField từ column JSON
                                        FormField columnField = new FormField();
                                        columnField.setId(columnNode.get("id").asText());
                                        columnField.setFieldKey(columnNode.get("key").asText());
                                        columnField.setLabel(columnNode.get("label").asText());
                                        columnField.setFieldType(FieldTypeEnum.CONTRIBUTED);
                                        columnField.setContributionConfig(columnContributionConfig);
                                        contributedFields.add(columnField);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (field.getFieldType() == FieldTypeEnum.SECTION && field.getChildren() != null) {
                // Xử lý SECTION fields - kiểm tra children
                JsonNode childrenNode = field.getChildren();
                if (childrenNode.isArray()) {
                    for (JsonNode childNode : childrenNode) {
                        JsonNode childTypeNode = childNode.get("type");
                        if (childTypeNode != null && "CONTRIBUTED".equals(childTypeNode.asText())) {
                            JsonNode childContributionConfig = childNode.get("contributionConfig");
                            if (childContributionConfig != null) {
                                JsonNode childTargetTemplateIds = childContributionConfig.get("targetTemplateIds");
                                if (childTargetTemplateIds != null && childTargetTemplateIds.isArray()) {
                                    boolean hasMatchingTarget = false;
                                    for (JsonNode targetId : childTargetTemplateIds) {
                                        if (targetTemplateIds.contains(targetId.asText())) {
                                            hasMatchingTarget = true;
                                            break;
                                        }
                                    }

                                    if (hasMatchingTarget) {
                                        // Tạo FormField từ child JSON
                                        FormField childField = new FormField();
                                        childField.setId(childNode.get("id").asText());
                                        childField.setFieldKey(childNode.get("fieldKey").asText());
                                        childField.setLabel(childNode.get("label").asText());
                                        childField.setFieldType(FieldTypeEnum.CONTRIBUTED);
                                        childField.setContributionConfig(childContributionConfig);
                                        contributedFields.add(childField);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return contributedFields;
    }

    /**
     * Tăng cường một template với contributed fields
     */
    private FormTemplateResponse enhanceTemplateWithContributedFields(FormTemplateResponse template,
            List<FormField> contributedFields) {
        // Tìm contributed fields có targetTemplateIds chứa template ID này
        List<FormField> matchingFields = contributedFields.stream()
                .filter(field -> {
                    if (field.getContributionConfig() != null) {
                        JsonNode targetTemplateIdsNode = field.getContributionConfig().get("targetTemplateIds");
                        if (targetTemplateIdsNode != null && targetTemplateIdsNode.isArray()) {
                            for (JsonNode targetId : targetTemplateIdsNode) {
                                if (template.getId().equals(targetId.asText())) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

        if (matchingFields.isEmpty()) {
            return template;
        }

        // Tạo bản sao của template và thêm contributed fields
        FormTemplateResponse enhancedTemplate = new FormTemplateResponse();
        enhancedTemplate.setId(template.getId());
        enhancedTemplate.setTemplateContent(template.getTemplateContent());
        enhancedTemplate.setFormFields(template.getFormFields());
        enhancedTemplate.setInnovationRoundId(template.getInnovationRoundId());
        enhancedTemplate.setTemplateType(template.getTemplateType());
        enhancedTemplate.setTargetRole(template.getTargetRole());
        enhancedTemplate.setCreatedAt(template.getCreatedAt());
        enhancedTemplate.setUpdatedAt(template.getUpdatedAt());

        // Thêm contributed fields vào danh sách fields
        List<FormFieldResponse> allFields = new ArrayList<>(template.getFormFields());

        // Convert FormField to FormFieldResponse và thêm vào
        for (FormField field : matchingFields) {
            FormFieldResponse fieldResponse = new FormFieldResponse();
            fieldResponse.setId(field.getId());
            fieldResponse.setFieldKey(field.getFieldKey());
            fieldResponse.setLabel(field.getLabel());
            fieldResponse.setFieldType(field.getFieldType());
            fieldResponse.setRequired(field.getRequired());
            fieldResponse.setIsReadOnly(field.getIsReadOnly());
            fieldResponse.setFormTemplateId(template.getId()); // Set formTemplateId từ template hiện tại
            fieldResponse.setContributionConfig(field.getContributionConfig());
            allFields.add(fieldResponse);
        }

        enhancedTemplate.setFormFields(allFields);

        return enhancedTemplate;
    }

    /**
     * Tự sinh UUID mới cho các column trong JsonNode tableConfig
     */
    private JsonNode generateColumnIdsIfNeeded(JsonNode tableConfig) {
        if (tableConfig != null && tableConfig.has("columns")) {
            try {
                ObjectNode tableConfigNode = (ObjectNode) tableConfig.deepCopy();
                ArrayNode columnsNode = (ArrayNode) tableConfigNode.get("columns");

                for (int i = 0; i < columnsNode.size(); i++) {
                    ObjectNode columnNode = (ObjectNode) columnsNode.get(i);
                    // Sinh UUID mới cho mỗi column
                    columnNode.put("id", UUID.randomUUID().toString());
                }

                return tableConfigNode;
            } catch (Exception e) {
                throw new IdInvalidException("Lỗi khi xử lý table config: " + e.getMessage());
            }
        }
        return tableConfig;
    }

    /**
     * Tự sinh UUID mới cho các children trong JsonNode children
     */
    private JsonNode generateChildrenIdsIfNeeded(JsonNode children) {
        if (children != null && children.isArray()) {
            try {
                ArrayNode childrenNode = (ArrayNode) children.deepCopy();

                for (int i = 0; i < childrenNode.size(); i++) {
                    ObjectNode childNode = (ObjectNode) childrenNode.get(i);
                    // Sinh UUID mới cho mỗi child
                    childNode.put("id", UUID.randomUUID().toString());
                }

                return childrenNode;
            } catch (Exception e) {
                throw new IdInvalidException("Lỗi khi xử lý children config: " + e.getMessage());
            }
        }
        return children;
    }
}
