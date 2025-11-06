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
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.NotFoundException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.HtmlTemplateUtils;

import java.util.List;
import java.util.Map;
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
        if (id == null || id.trim().isEmpty()) {
            throw new IdInvalidException("ID không được để trống");
        }

        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Form template không tồn tại với ID: " + id));

        return formTemplateMapper.toCreateTemplateWithFieldsResponse(template);
    }

    // 2. Lấy tất cả form templates theo innovation round hiện tại
    public List<FormTemplateResponse> getFormTemplatesByCurrentRound() {
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(
                LocalDate.now(), InnovationRoundStatusEnum.OPEN)
                .orElse(null);

        List<FormTemplate> templates;

        if (currentRound != null) {
            // Có current round - lấy templates của round đó
            templates = formTemplateRepository
                    .findByInnovationRoundIdOrderByTemplateType(currentRound.getId());

            // if (templates.isEmpty()) {
            // templates = formTemplateRepository
            // .findByInnovationRoundIsNullOrderByTemplateType();
            // }
            if (templates.isEmpty()) {
                throw new NotFoundException("Không tìm thấy templates cho round hiện tại");
            }
        } else {
            // Không có current round - lấy templates từ template library
            templates = formTemplateRepository
                    .findByInnovationRoundIsNullOrderByTemplateType();
        }

        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 3. Cập nhật form template
    @Transactional
    public FormTemplateResponse updateFormTemplate(String id, UpdateFormTemplateRequest request) {
        if (id == null || id.trim().isEmpty()) {
            throw new IdInvalidException("ID không được để trống");
        }

        if (request == null) {
            throw new IdInvalidException("Request không được null");
        }

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
        if (request == null) {
            throw new IdInvalidException("Request không được null");
        }

        if (request.getRoundId() == null || request.getRoundId().trim().isEmpty()) {
            throw new IdInvalidException("RoundId không được để trống");
        }

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
    @Transactional
    public void deleteFormTemplate(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IdInvalidException("ID không được để trống");
        }

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

        if (template == null) {
            throw new IdInvalidException("Template không được null");
        }

        Map<String, FormField> existingById = template.getFormFields().stream()
                .filter(f -> f.getId() != null)
                .collect(Collectors.toMap(FormField::getId, f -> f));

        // Tạo list mới theo đúng thứ tự của request
        List<FormField> orderedFields = new ArrayList<>();
        for (FormFieldRequest fd : fields) {
            FormField entity = null;

            // Ưu tiên tìm theo ID trước
            if (fd.getId() != null && existingById.containsKey(fd.getId())) {
                // Field đã tồn tại, cập nhật trực tiếp
                entity = existingById.get(fd.getId());
            } else {
                // Nếu không có ID hoặc ID không tồn tại, tạo field mới
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

            // tableConfig/options/children: mapper như create
            if (fd.getType() == FieldTypeEnum.TABLE && fd.getTableConfig() != null) {
                // Tự sinh UUID cho các column nếu chưa có hoặc ID không phải UUID hợp lệ
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

            // Add vào list theo thứ tự
            orderedFields.add(entity);
        }

        // Thay thế toàn bộ list formFields bằng list mới theo đúng thứ tự
        template.getFormFields().clear();
        template.getFormFields().addAll(orderedFields);
    }

    private void updateHtmlTemplateContent(FormTemplate template) {
        if (template == null) {
            throw new IdInvalidException("Template không được null");
        }

        if (template.getTemplateContent() == null || template.getFormFields().isEmpty()) {
            return;
        }

        try {
            String htmlContent = Utils.decode(template.getTemplateContent());
            String updatedHtmlContent = htmlTemplateUtils.updateFieldIdsInHtml(htmlContent, template.getFormFields());
            String encodedContent = Utils.encode(updatedHtmlContent);
            template.setTemplateContent(encodedContent);

        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi cập nhật HTML template content: " + e.getMessage());
        }
    }

    // 8. Lấy FormTemplate (InnovationRoundId = null) với pagination, filtering - OK
    @Transactional
    public CreateTemplateResponse createTemplate(CreateTemplateRequest request) {
        if (request == null) {
            throw new IdInvalidException("Request không được null");
        }

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
            throw new IdInvalidException("Lỗi khi tạo template: " + e.getMessage());
        }
    }

    // 9. Lấy form templates theo innovation round ID
    public List<FormTemplateResponse> getFormTemplatesByInnovationRound(String roundId) {
        if (roundId == null || roundId.trim().isEmpty()) {
            throw new IdInvalidException("Round ID không được để trống");
        }

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
        if (targetRole == null || targetRole.trim().isEmpty()) {
            throw new IdInvalidException("Target role không được để trống");
        }

        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(
                LocalDate.now(), InnovationRoundStatusEnum.OPEN)
                .orElse(null);

        TargetRoleCode targetRoleEnum;
        try {
            targetRoleEnum = TargetRoleCode.valueOf(targetRole);
        } catch (IllegalArgumentException e) {
            throw new IdInvalidException("Target role không hợp lệ: " + targetRole);
        }

        List<FormTemplate> templates;

        if (currentRound != null) {
            // Có current round - lấy templates của round đó
            templates = formTemplateRepository
                    .findByInnovationRoundIdAndTargetRoleOrderByTemplateType(currentRound.getId(), targetRoleEnum);

            // if (templates.isEmpty()) {
            // templates = formTemplateRepository
            // .findByInnovationRoundIsNullAndTargetRoleOrderByTemplateType(targetRoleEnum);
            // }
            if (templates.isEmpty()) {
                throw new NotFoundException("Không tìm thấy templates cho round hiện tại");
            }
        } else {
            // Không có current round - lấy templates từ template library
            templates = formTemplateRepository
                    .findByInnovationRoundIsNullAndTargetRoleOrderByTemplateType(targetRoleEnum);
        }

        return templates.stream()
                .map(formTemplateMapper::toFormTemplateResponse)
                .collect(Collectors.toList());
    }

    // 11. Lấy form templates theo roles của user hiện tại
    public List<FormTemplateResponse> getFormTemplatesByCurrentUserRoles() {
        UserResponse currentUser = userService.getCurrentUserResponse();
        List<String> userRoles = currentUser.getRoles();

        String targetRole = determineTargetRoleFromUserRoles(userRoles);

        List<FormTemplateResponse> templates = getFormTemplatesByCurrentRoundAndTargetRole(targetRole);

        // Thêm logic quét CONTRIBUTED fields từ templates 3,4,5
        return enhanceTemplatesWithContributedFields(templates);
    }

    private String determineTargetRoleFromUserRoles(List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return TargetRoleCode.EMPLOYEE.getValue();
        }

        for (String role : userRoles) {
            switch (role) {
                case "QUAN_TRI_VIEN_QLKH_HTQT":
                    return TargetRoleCode.QLKH_SECRETARY.getValue();
                case "TRUONG_KHOA":
                    return TargetRoleCode.DEPARTMENT.getValue();
                case "GIANG_VIEN":
                    return "EMPLOYEE";
            }
        }
        return TargetRoleCode.EMPLOYEE.getValue();
    }

    /**
     * Add templates với CONTRIBUTED fields từ templates 3,4,5
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
        return templates.stream()
                .map(template -> enhanceTemplateWithContributedFields(template, contributedFields))
                .collect(Collectors.toList());
    }

    /**
     * Lấy CONTRIBUTED fields từ templates 3,4,5 có targetTemplateIds khớp
     */
    private List<FormField> getContributedFieldsFromTemplates345(List<String> targetTemplateIds) {
        InnovationRound currentRound = innovationRoundRepository.findCurrentActiveRound(
                LocalDate.now(), InnovationRoundStatusEnum.OPEN)
                .orElse(null);

        List<FormTemplate> templates345;
        if (currentRound != null) {
            templates345 = formTemplateRepository.findByInnovationRoundIdAndTemplateTypeIn(
                    currentRound.getId(),
                    List.of(TemplateTypeEnum.BIEN_BAN_HOP, TemplateTypeEnum.TONG_HOP_DE_NGHI,
                            TemplateTypeEnum.TONG_HOP_CHAM_DIEM));

            // Nếu không có template trong round hiện tại, lấy từ template library
            if (templates345.isEmpty()) {
                templates345 = formTemplateRepository.findByInnovationRoundIsNullAndTemplateTypeIn(
                        List.of(TemplateTypeEnum.BIEN_BAN_HOP, TemplateTypeEnum.TONG_HOP_DE_NGHI,
                                TemplateTypeEnum.TONG_HOP_CHAM_DIEM));
            }
        } else {
            templates345 = formTemplateRepository.findByInnovationRoundIsNullAndTemplateTypeIn(
                    List.of(TemplateTypeEnum.BIEN_BAN_HOP, TemplateTypeEnum.TONG_HOP_DE_NGHI,
                            TemplateTypeEnum.TONG_HOP_CHAM_DIEM));
        }

        // Lấy tất cả CONTRIBUTED fields từ tất cả templates 3,4,5
        List<FormField> allContributedFields = templates345.stream()
                .flatMap(template -> extractContributedFields(template, targetTemplateIds).stream())
                .collect(Collectors.toList());

        return allContributedFields;
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
                                        // Set formTemplate ID gốc
                                        columnField.setFormTemplate(template);
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
                                        // Set formTemplate ID gốc
                                        childField.setFormTemplate(template);
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
     * Add một template với contributed fields
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
            // Giữ nguyên formTemplateId gốc từ field (templates 3,4,5)
            fieldResponse.setFormTemplateId(field.getFormTemplate() != null ? field.getFormTemplate().getId() : null);
            fieldResponse.setContributionConfig(field.getContributionConfig());
            allFields.add(fieldResponse);
        }

        enhancedTemplate.setFormFields(allFields);

        return enhancedTemplate;
    }

    /**
     * Tự sinh UUID mới cho các column trong JsonNode tableConfig
     * Thay thế các ID giả (không phải UUID format) bằng UUID mới
     */
    private JsonNode generateColumnIdsIfNeeded(JsonNode tableConfig) {
        if (tableConfig != null && tableConfig.has("columns")) {
            try {
                ObjectNode tableConfigNode = (ObjectNode) tableConfig.deepCopy();
                ArrayNode columnsNode = (ArrayNode) tableConfigNode.get("columns");

                for (int i = 0; i < columnsNode.size(); i++) {
                    ObjectNode columnNode = (ObjectNode) columnsNode.get(i);
                    // Sinh UUID mới nếu column chưa có ID hoặc ID không phải UUID hợp lệ
                    if (!columnNode.has("id") || columnNode.get("id").isNull()
                            || columnNode.get("id").asText().isEmpty()
                            || !isValidUUID(columnNode.get("id").asText())) {
                        columnNode.put("id", UUID.randomUUID().toString());
                    }
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
     * Thay thế các ID giả (không phải UUID format) bằng UUID mới
     */
    private JsonNode generateChildrenIdsIfNeeded(JsonNode children) {
        if (children != null && children.isArray()) {
            try {
                ArrayNode childrenNode = (ArrayNode) children.deepCopy();

                for (int i = 0; i < childrenNode.size(); i++) {
                    ObjectNode childNode = (ObjectNode) childrenNode.get(i);
                    // Sinh UUID mới nếu child chưa có ID hoặc ID không phải UUID hợp lệ
                    if (!childNode.has("id") || childNode.get("id").isNull()
                            || childNode.get("id").asText().isEmpty()
                            || !isValidUUID(childNode.get("id").asText())) {
                        childNode.put("id", UUID.randomUUID().toString());
                    }
                }

                return childrenNode;
            } catch (Exception e) {
                throw new IdInvalidException("Lỗi khi xử lý children config: " + e.getMessage());
            }
        }
        return children;
    }

    /**
     * Kiểm tra xem một chuỗi có phải là UUID hợp lệ không
     */
    private boolean isValidUUID(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
