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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Council;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;
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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SecretarySummaryTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CouncilResultsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationResultDetail;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.InnovationFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFormDataResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFieldResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.NotFoundException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.HtmlTemplateUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
    private final CouncilService councilService;
    private final InnovationService innovationService;
    private final CouncilRepository councilRepository;
    private final InnovationRepository innovationRepository;
    private final ObjectMapper objectMapper;

    public FormTemplateService(FormTemplateRepository formTemplateRepository,
            InnovationRoundRepository innovationRoundRepository,
            FormTemplateMapper formTemplateMapper,
            HtmlTemplateUtils htmlTemplateUtils,
            UserService userService,
            CouncilService councilService,
            InnovationService innovationService,
            CouncilRepository councilRepository,
            InnovationRepository innovationRepository,
            ObjectMapper objectMapper) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationRoundRepository = innovationRoundRepository;
        this.formTemplateMapper = formTemplateMapper;
        this.htmlTemplateUtils = htmlTemplateUtils;
        this.userService = userService;
        this.councilService = councilService;
        this.innovationService = innovationService;
        this.councilRepository = councilRepository;
        this.innovationRepository = innovationRepository;
        this.objectMapper = objectMapper;
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
                .and((root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isLibrary")));

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
                                        columnField.setRequired(columnNode.has("required")
                                                ? columnNode.get("required").asBoolean()
                                                : Boolean.FALSE);
                                        columnField.setIsReadOnly(columnNode.has("isReadOnly")
                                                ? columnNode.get("isReadOnly").asBoolean()
                                                : Boolean.FALSE);
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
                                        childField.setRequired(childNode.has("required")
                                                ? childNode.get("required").asBoolean()
                                                : Boolean.FALSE);
                                        childField.setIsReadOnly(childNode.has("isReadOnly")
                                                ? childNode.get("isReadOnly").asBoolean()
                                                : Boolean.FALSE);
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

    // ==================== SECRETARY SUMMARY TEMPLATE ====================

    /**
     * Check xem current user có phải là thư ký của council không
     */
    private boolean isCurrentUserSecretary(String councilId) {
        User currentUser = userService.getCurrentUser();
        Council council = councilRepository.findById(councilId)
                .orElse(null);
        
        if (council == null) {
            return false;
        }
        
        // Check xem user có là THU_KY trong council này không
        return council.getCouncilMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUser.getId()) &&
                        member.getRole() == CouncilMemberRoleEnum.THU_KY);
    }

    /**
     * Lấy template tổng hợp cho thư ký (Mẫu 3, 4, 5) với data đã được build động
     */
    public SecretarySummaryTemplateResponse getSecretarySummaryTemplate(
            String councilId,
            TemplateTypeEnum templateType) {
        
        // 1. Validate templateType
        if (templateType != TemplateTypeEnum.BIEN_BAN_HOP &&
            templateType != TemplateTypeEnum.TONG_HOP_DE_NGHI &&
            templateType != TemplateTypeEnum.TONG_HOP_CHAM_DIEM) {
            throw new IdInvalidException("Template type phải là BIEN_BAN_HOP, TONG_HOP_DE_NGHI, hoặc TONG_HOP_CHAM_DIEM");
        }

        // 2. Validate user là thư ký
        if (!isCurrentUserSecretary(councilId)) {
            throw new IdInvalidException("Bạn không phải thư ký của hội đồng này");
        }

        // 3. Lấy council và roundId
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hội đồng với ID: " + councilId));
        String roundId = council.getInnovationRound().getId();

        // 4. Xác định filter type
        Boolean isScoreFilter = null;
        if (templateType == TemplateTypeEnum.TONG_HOP_DE_NGHI) {
            isScoreFilter = false; // Chỉ innovations không chấm điểm
        } else if (templateType == TemplateTypeEnum.TONG_HOP_CHAM_DIEM) {
            isScoreFilter = true; // Chỉ innovations có chấm điểm
        }
        // Template 3: isScoreFilter = null (tất cả)

        // 5. Lấy template
        List<FormTemplate> templates = formTemplateRepository
                .findByInnovationRoundIdAndTemplateTypeIn(roundId, List.of(templateType));
        FormTemplate template = templates.isEmpty() ? null : templates.get(0);

        if (template == null) {
            List<FormTemplate> libraryTemplates = formTemplateRepository
                    .findByInnovationRoundIsNullAndTemplateTypeIn(List.of(templateType));
            if (libraryTemplates.isEmpty()) {
                throw new NotFoundException("Không tìm thấy mẫu " + templateType.getValue());
            }
            template = libraryTemplates.get(0);
        }

        // 6. Map template sang response
        CreateTemplateWithFieldsResponse templateResponse = formTemplateMapper.toCreateTemplateWithFieldsResponse(template);

        // 7. Build field data map động
        Map<String, Object> fieldDataMap = buildFieldDataMapDynamic(template, councilId, isScoreFilter);

        // 8. Tạo response
        SecretarySummaryTemplateResponse response = new SecretarySummaryTemplateResponse();
        // Copy tất cả fields từ templateResponse
        response.setId(templateResponse.getId());
        response.setTemplateContent(templateResponse.getTemplateContent());
        response.setTemplateType(templateResponse.getTemplateType());
        response.setTargetRole(templateResponse.getTargetRole());
        response.setIsLibrary(templateResponse.getIsLibrary());
        response.setRoundId(templateResponse.getRoundId());
        response.setFields(templateResponse.getFields());
        response.setCreatedAt(templateResponse.getCreatedAt());
        response.setUpdatedAt(templateResponse.getUpdatedAt());
        response.setCreatedBy(templateResponse.getCreatedBy());
        response.setUpdatedBy(templateResponse.getUpdatedBy());

        // Set field data map
        response.setFieldDataMap(fieldDataMap);

        return response;
    }

    /**
     * Build field data map động - xử lý tất cả field types
     */
    private Map<String, Object> buildFieldDataMapDynamic(
            FormTemplate template,
            String councilId,
            Boolean isScoreFilter) {
        
        Map<String, Object> fieldDataMap = new HashMap<>();

        try {
            // Lấy council results
            CouncilResultsResponse councilResults = councilService.getCouncilResults(councilId);
            List<InnovationResultDetail> filteredResults = filterInnovationsByScore(
                    councilResults.getInnovationResults(),
                    isScoreFilter);

            // Tìm TẤT CẢ fields cần data (recursive)
            List<FieldDataConfig> fieldsNeedingData = findAllFieldsNeedingData(template.getFormFields());

            // Build data cho từng field
            for (FieldDataConfig fieldConfig : fieldsNeedingData) {
                Object fieldData = buildDataForField(fieldConfig, filteredResults, councilId);
                if (fieldData != null) {
                    fieldDataMap.put(fieldConfig.fieldKey, fieldData);
                }
            }

        } catch (IllegalArgumentException e) {
            // Nếu chưa hết thời gian chấm điểm, trả về empty map
            log.warn("Chưa hết thời gian chấm điểm cho council: " + councilId);
        }

        return fieldDataMap;
    }

    /**
     * Filter innovations dựa trên isScore
     */
    private List<InnovationResultDetail> filterInnovationsByScore(
            List<InnovationResultDetail> results,
            Boolean isScoreFilter) {
        
        if (isScoreFilter == null) {
            return results; // Template 3: Tất cả innovations
        }

        return results.stream()
                .filter(result -> {
                    Boolean isScore = result.getIsScore();
                    if (isScoreFilter == false) {
                        // Template 4: Chỉ lấy không chấm điểm
                        return isScore == null || isScore == false;
                    } else {
                        // Template 5: Chỉ lấy có chấm điểm
                        return isScore != null && isScore == true;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Tìm TẤT CẢ fields cần data (recursive)
     */
    private List<FieldDataConfig> findAllFieldsNeedingData(List<FormField> fields) {
        List<FieldDataConfig> fieldsNeedingData = new ArrayList<>();

        for (FormField field : fields) {
            FieldTypeEnum fieldType = field.getFieldType();

            // SECTION field: Kiểm tra children có data fields không
            if (fieldType == FieldTypeEnum.SECTION) {
                List<ChildFieldConfig> childConfigs = extractChildFieldConfigs(field);
                boolean hasDataFields = childConfigs.stream().anyMatch(config ->
                        config.fieldType.equals("INNOVATION_DATA") ||
                        config.fieldType.equals("USER_DATA") ||
                        config.fieldType.equals("REFERENCE") ||
                        config.fieldType.equals("CONTRIBUTED"));

                if (hasDataFields) {
                    FieldDataConfig fieldConfig = new FieldDataConfig(field.getFieldKey(), FieldTypeEnum.SECTION);
                    fieldConfig.isRepeatable = Boolean.TRUE.equals(field.getRepeatable());
                    fieldConfig.childConfigs = childConfigs;
                    fieldsNeedingData.add(fieldConfig);
                }
            }
            // TABLE field: Kiểm tra columns có data columns không
            else if (fieldType == FieldTypeEnum.TABLE && field.getTableConfig() != null) {
                List<TableColumnConfig> tableColumns = extractTableColumnConfigs(field);
                boolean hasDataColumns = tableColumns.stream().anyMatch(config ->
                        config.columnType.equals("INNOVATION_DATA") ||
                        config.columnType.equals("USER_DATA") ||
                        config.columnType.equals("REFERENCE") ||
                        config.columnType.equals("CONTRIBUTED"));

                if (hasDataColumns) {
                    FieldDataConfig fieldConfig = new FieldDataConfig(field.getFieldKey(), FieldTypeEnum.TABLE);
                    fieldConfig.tableColumns = tableColumns;
                    fieldsNeedingData.add(fieldConfig);
                }
            }

            // Nested fields: Nếu có children, tìm recursive
            if (field.getChildren() != null && field.getChildren().isArray()) {
                List<FormField> childrenFields = parseChildrenFields(field.getChildren());
                List<FieldDataConfig> nestedFields = findAllFieldsNeedingData(childrenFields);
                fieldsNeedingData.addAll(nestedFields);
            }
        }

        return fieldsNeedingData;
    }

    /**
     * Build data cho một field cụ thể
     */
    private Object buildDataForField(
            FieldDataConfig fieldConfig,
            List<InnovationResultDetail> filteredResults,
            String councilId) {
        
        switch (fieldConfig.fieldType) {
            case SECTION:
                // Build array of instance data
                List<Map<String, Object>> instanceDataList = new ArrayList<>();

                for (InnovationResultDetail result : filteredResults) {
                    Map<String, Object> instanceData = new HashMap<>();

                    // Resolve từng child field
                    for (ChildFieldConfig childConfig : fieldConfig.childConfigs) {
                        Object value = resolveChildFieldValue(childConfig, result, councilId);
                        // Luôn put vào map, kể cả null (cho fields thường)
                        instanceData.put(childConfig.fieldKey, value);
                    }

                    instanceDataList.add(instanceData);
                }

                return instanceDataList;

            case TABLE:
                // Build array of row data
                List<Map<String, Object>> rowDataList = new ArrayList<>();

                for (InnovationResultDetail result : filteredResults) {
                    Map<String, Object> rowData = new HashMap<>();

                    // Resolve từng table column
                    for (TableColumnConfig columnConfig : fieldConfig.tableColumns) {
                        Object value = resolveTableColumnValue(columnConfig, result, councilId);
                        // Luôn put vào map, kể cả null
                        rowData.put(columnConfig.columnKey, value);
                    }

                    rowDataList.add(rowData);
                }

                return rowDataList;

            default:
                return null;
        }
    }

    /**
     * Resolve value cho child field
     */
    private Object resolveChildFieldValue(
            ChildFieldConfig childConfig,
            InnovationResultDetail result,
            String councilId) {
        
        switch (childConfig.fieldType) {
            case "INNOVATION_DATA":
                if (childConfig.sourceFieldKey != null) {
                    return getInnovationDataValue(result, childConfig.sourceFieldKey);
                }
                break;

            case "USER_DATA":
                if (childConfig.sourceFieldKey != null) {
                    return getUserDataValue(result, childConfig.sourceFieldKey);
                }
                break;

            case "REFERENCE":
                if (childConfig.referenceConfig != null) {
                    return resolveReferenceFieldValue(childConfig.referenceConfig, result.getInnovationId());
                }
                break;

            case "CONTRIBUTED":
                if (childConfig.targetTemplateIds != null && !childConfig.targetTemplateIds.isEmpty()) {
                    return getContributedFieldValue(childConfig.targetTemplateIds, childConfig.fieldKey, result.getInnovationId());
                }
                break;

            // TEXT, LONG_TEXT, NUMBER, etc. để null
            default:
                return null;
        }

        return null;
    }

    /**
     * Resolve value cho table column
     */
    private Object resolveTableColumnValue(
            TableColumnConfig columnConfig,
            InnovationResultDetail result,
            String councilId) {
        
        switch (columnConfig.columnType) {
            case "INNOVATION_DATA":
                if (columnConfig.sourceFieldKey != null) {
                    return getInnovationDataValue(result, columnConfig.sourceFieldKey);
                }
                break;

            case "USER_DATA":
                if (columnConfig.sourceFieldKey != null) {
                    return getUserDataValue(result, columnConfig.sourceFieldKey);
                }
                break;

            case "REFERENCE":
                if (columnConfig.referenceConfig != null) {
                    return resolveReferenceFieldValue(columnConfig.referenceConfig, result.getInnovationId());
                }
                break;

            case "CONTRIBUTED":
                if (columnConfig.targetTemplateIds != null && !columnConfig.targetTemplateIds.isEmpty()) {
                    return getContributedFieldValue(columnConfig.targetTemplateIds, columnConfig.columnKey, result.getInnovationId());
                }
                break;

            default:
                return null;
        }

        return null;
    }

    /**
     * Get innovation data value
     */
    private Object getInnovationDataValue(InnovationResultDetail result, String sourceFieldKey) {
        switch (sourceFieldKey) {
            case "innovationName":
                return result.getInnovationName();
            case "authorName":
                return result.getAuthorName();
            case "finalDecision":
                return result.getFinalDecision();
            case "decisionReason":
                return result.getDecisionReason();
            case "averageScore":
                return result.getAverageScore();
            case "isScore":
                return result.getIsScore() != null && result.getIsScore() ? "Có chấm điểm" : "Không chấm điểm";
            case "departmentName":
                return result.getDepartmentName();
            default:
                log.warn("Unknown innovation data sourceFieldKey: " + sourceFieldKey);
                return null;
        }
    }

    /**
     * Get user data value (từ author)
     */
    private Object getUserDataValue(InnovationResultDetail result, String sourceFieldKey) {
        try {
            // Lấy Innovation entity để có thông tin user đầy đủ
            Innovation innovation = innovationRepository.findById(result.getInnovationId())
                    .orElse(null);
            
            if (innovation == null || innovation.getUser() == null) {
                // Fallback: chỉ có authorName từ result
                if ("fullName".equals(sourceFieldKey)) {
                    return result.getAuthorName();
                }
                return null;
            }
            
            User author = innovation.getUser();
            
            switch (sourceFieldKey) {
                case "fullName":
                    return author.getFullName();
                case "dateOfBirth":
                    return author.getDateOfBirth() != null ? author.getDateOfBirth().toString() : null;
                case "department":
                    return author.getDepartment() != null ? author.getDepartment().getDepartmentName() : null;
                case "title":
                    return author.getTitle();
                case "qualification":
                    return author.getQualification();
                case "email":
                    return author.getEmail();
                default:
                    log.warn("Unknown user data sourceFieldKey: " + sourceFieldKey);
                    return null;
            }
        } catch (Exception e) {
            log.error("Error getting user data value for innovation: " + result.getInnovationId(), e);
            // Fallback: chỉ có authorName từ result
            if ("fullName".equals(sourceFieldKey)) {
                return result.getAuthorName();
            }
            return null;
        }
    }

    /**
     * Resolve REFERENCE field value
     * Hỗ trợ nested path như "tableKey.columnKey" cho TABLE fields
     */
    private String resolveReferenceFieldValue(JsonNode referenceConfig, String innovationId) {
        try {
            String sourceTemplateId = referenceConfig.get("sourceTemplateId").asText();
            JsonNode sourceFieldKeysNode = referenceConfig.get("sourceFieldKeys");
            String displayFormat = referenceConfig.has("displayFormat")
                    ? referenceConfig.get("displayFormat").asText()
                    : null;
            String multiRowSeparator = referenceConfig.has("multiRowSeparator")
                    ? referenceConfig.get("multiRowSeparator").asText()
                    : " ";

            List<String> sourceFieldKeys = new ArrayList<>();
            if (sourceFieldKeysNode != null && sourceFieldKeysNode.isArray()) {
                for (JsonNode keyNode : sourceFieldKeysNode) {
                    sourceFieldKeys.add(keyNode.asText());
                }
            }

            // Lấy innovation form data
            InnovationFormDataResponse innovationFormData = innovationService.getInnovationWithFormDataById(innovationId);

            if (innovationFormData == null || innovationFormData.getTemplates() == null) {
                return "[Không tìm thấy dữ liệu từ " + sourceTemplateId + "]";
            }

            // Tìm template data tương ứng với sourceTemplateId
            Map<String, Object> sourceData = null;
            List<TemplateFieldResponse> templateFields = null;
            for (var template : innovationFormData.getTemplates()) {
                if (template.getTemplateId().equals(sourceTemplateId)) {
                    // Convert fields sang Map<String, Object>
                    sourceData = convertTemplateFieldsToMap(template.getFields());
                    templateFields = template.getFields();
                    break;
                }
            }

            if (sourceData == null || templateFields == null) {
                return "[Không tìm thấy dữ liệu từ " + sourceTemplateId + "]";
            }

            // Lấy giá trị từ các field (hỗ trợ nested path cho TABLE)
            List<String> values = new ArrayList<>();
            for (String fieldKey : sourceFieldKeys) {
                String value = getNestedFieldValue(sourceData, templateFields, fieldKey, multiRowSeparator);
                values.add(value != null ? value : "");
            }

            // Format dữ liệu
            if (values.stream().allMatch(v -> v.trim().isEmpty())) {
                return "";
            }

            if (displayFormat != null && !displayFormat.isEmpty()) {
                String result = displayFormat;
                for (int i = 0; i < sourceFieldKeys.size(); i++) {
                    // Trong displayFormat, có thể dùng {ho_va_ten} thay vì {danh_sach_tac_gia.ho_va_ten}
                    String keyForFormat = sourceFieldKeys.get(i);
                    // Nếu là nested path, lấy phần sau dấu chấm
                    if (keyForFormat.contains(".")) {
                        keyForFormat = keyForFormat.substring(keyForFormat.lastIndexOf(".") + 1);
                    }
                    result = result.replace("{" + keyForFormat + "}", values.get(i));
                    // Cũng thử replace với full path
                    result = result.replace("{" + sourceFieldKeys.get(i) + "}", values.get(i));
                }
                return result;
            }

            return String.join(multiRowSeparator, values);

        } catch (Exception e) {
            log.error("Error resolving REFERENCE field value for innovation: " + innovationId, e);
            return "[Lỗi khi lấy dữ liệu tham chiếu]";
        }
    }

    /**
     * Lấy giá trị từ sourceData, hỗ trợ nested path (tableKey.columnKey)
     * @param sourceData Map từ convertTemplateFieldsToMap (key là label)
     * @param templateFields List TemplateFieldResponse để tìm fieldKey -> label mapping
     * @param fieldKey Có thể là "fieldKey" hoặc "tableKey.columnKey" (fieldKey của TABLE field)
     * @param multiRowSeparator Separator để join các rows trong table (thường là "\n")
     */
    private String getNestedFieldValue(Map<String, Object> sourceData, List<TemplateFieldResponse> templateFields, String fieldKey, String multiRowSeparator) {
        // Kiểm tra xem có phải nested path không (table.column)
        if (fieldKey.contains(".")) {
            String[] parts = fieldKey.split("\\.", 2);
            String tableKey = parts[0]; // fieldKey của TABLE field (ví dụ: "danh_sach_tac_gia")
            String columnKey = parts[1]; // column key trong table (ví dụ: "ho_va_ten")
            
            // Tìm label của TABLE field từ fieldKey
            // Vì TemplateFieldResponse không có fieldKey, tìm trong sourceData keys bằng fuzzy match
            String tableLabel = null;
            
            // Thử tìm trực tiếp bằng tableKey (nếu tableKey là label)
            if (sourceData.containsKey(tableKey)) {
                tableLabel = tableKey;
            } else {
                // Tìm bằng fuzzy match trong sourceData keys
                // Tìm key có chứa tableKey hoặc ngược lại
                tableLabel = sourceData.keySet().stream()
                        .filter(k -> {
                            String kLower = k.toLowerCase().replaceAll("[\\s_]", "");
                            String tableKeyLower = tableKey.toLowerCase().replaceAll("[\\s_]", "");
                            return kLower.contains(tableKeyLower) || tableKeyLower.contains(kLower) ||
                                   k.toLowerCase().contains(tableKey.toLowerCase()) ||
                                   tableKey.toLowerCase().contains(k.toLowerCase());
                        })
                        .findFirst()
                        .orElse(null);
            }
            
            if (tableLabel == null) {
                log.warn("Không tìm thấy table field với key: " + tableKey);
                return null;
            }
            
            // Lấy table data
            Object tableData = sourceData.get(tableLabel);
            
            // Nếu tableData là List (TABLE field), extract column values
            if (tableData instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tableRows = (List<Map<String, Object>>) tableData;
                List<String> columnValues = new ArrayList<>();
                
                for (Map<String, Object> row : tableRows) {
                    Object columnValue = row.get(columnKey);
                    // Nếu không tìm thấy bằng columnKey, thử tìm bằng label (fuzzy match)
                    if (columnValue == null) {
                        String similarColKey = row.keySet().stream()
                                .filter(k -> {
                                    String kLower = k.toLowerCase().replaceAll("[\\s_]", "");
                                    String colKeyLower = columnKey.toLowerCase().replaceAll("[\\s_]", "");
                                    return kLower.contains(colKeyLower) || colKeyLower.contains(kLower) ||
                                           k.toLowerCase().contains(columnKey.toLowerCase()) ||
                                           columnKey.toLowerCase().contains(k.toLowerCase());
                                })
                                .findFirst()
                                .orElse(null);
                        columnValue = similarColKey != null ? row.get(similarColKey) : null;
                    }
                    
                    if (columnValue != null) {
                        columnValues.add(columnValue.toString());
                    }
                }
                
                // Join các giá trị bằng multiRowSeparator (thường là "\n" cho table rows)
                String separator = (multiRowSeparator != null && !multiRowSeparator.isEmpty()) ? multiRowSeparator : "\n";
                return String.join(separator, columnValues);
            }
            
            log.warn("Table field '" + tableLabel + "' không phải là List (TABLE field)");
            return null;
        }
        
        // Nếu không phải nested path, lấy giá trị trực tiếp
        Object value = sourceData.get(fieldKey);
        if (value == null) {
            // Tìm key tương tự (fuzzy match)
            String similarKey = sourceData.keySet().stream()
                    .filter(k -> {
                        String kLower = k.toLowerCase().replaceAll("[\\s_]", "");
                        String fieldKeyLower = fieldKey.toLowerCase().replaceAll("[\\s_]", "");
                        return kLower.contains(fieldKeyLower) || fieldKeyLower.contains(kLower) ||
                               k.toLowerCase().contains(fieldKey.toLowerCase()) ||
                               fieldKey.toLowerCase().contains(k.toLowerCase());
                    })
                    .findFirst()
                    .orElse(null);
            value = similarKey != null ? sourceData.get(similarKey) : null;
        }
        
        return value != null ? value.toString() : null;
    }

    /**
     * Lấy CONTRIBUTED field value từ formData
     * Tìm trong tất cả targetTemplateIds
     */
    private Object getContributedFieldValue(List<String> targetTemplateIds, String fieldKey, String innovationId) {
        try {
            // Lấy innovation form data
            InnovationFormDataResponse innovationFormData = innovationService.getInnovationWithFormDataById(innovationId);

            if (innovationFormData == null || innovationFormData.getTemplates() == null) {
                return null;
            }

            // Tìm trong tất cả targetTemplateIds
            for (String targetTemplateId : targetTemplateIds) {
                for (var template : innovationFormData.getTemplates()) {
                    if (template.getTemplateId().equals(targetTemplateId)) {
                        // Convert fields sang Map
                        Map<String, Object> formData = convertTemplateFieldsToMap(template.getFields());
                        Object value = formData.get(fieldKey);
                                                   
                        // Nếu tìm thấy value, return ngay
                        if (value != null) {
                            return value;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error getting CONTRIBUTED field value for innovation: " + innovationId, e);
        }

        return null;
    }

    /**
     * Extract child field configs từ SECTION field
     */
    private List<ChildFieldConfig> extractChildFieldConfigs(FormField sectionField) {
        List<ChildFieldConfig> configs = new ArrayList<>();

        if (sectionField.getChildren() == null || !sectionField.getChildren().isArray()) {
            return configs;
        }

        try {
            for (JsonNode childNode : sectionField.getChildren()) {
                JsonNode typeNode = childNode.get("type");
                JsonNode fieldKeyNode = childNode.get("fieldKey");

                if (typeNode == null || fieldKeyNode == null) continue;

                String fieldType = typeNode.asText();
                String fieldKey = fieldKeyNode.asText();

                ChildFieldConfig config = new ChildFieldConfig(fieldKey, fieldType);

                // Extract config dựa trên field type
                switch (fieldType) {
                    case "INNOVATION_DATA":
                        JsonNode innovationDataConfig = childNode.get("innovationDataConfig");
                        if (innovationDataConfig != null) {
                            JsonNode sourceFieldKeyNode = innovationDataConfig.get("sourceFieldKey");
                            if (sourceFieldKeyNode != null) {
                                config.sourceFieldKey = sourceFieldKeyNode.asText();
                            }
                        }
                        break;

                    case "USER_DATA":
                        JsonNode userDataConfig = childNode.get("userDataConfig");
                        if (userDataConfig != null) {
                            JsonNode sourceFieldKeyNode = userDataConfig.get("sourceFieldKey");
                            if (sourceFieldKeyNode != null) {
                                config.sourceFieldKey = sourceFieldKeyNode.asText();
                            }
                        }
                        break;

                    case "REFERENCE":
                        JsonNode referenceConfigNode = childNode.get("referenceConfig");
                        if (referenceConfigNode != null) {
                            config.referenceConfig = referenceConfigNode;
                        }
                        break;

                    case "CONTRIBUTED":
                        // CONTRIBUTED có contributionConfig với targetTemplateIds (mảng)
                        JsonNode contributionConfigNode = childNode.get("contributionConfig");
                        if (contributionConfigNode != null) {
                            JsonNode targetTemplateIdsNode = contributionConfigNode.get("targetTemplateIds");
                            if (targetTemplateIdsNode != null && targetTemplateIdsNode.isArray()) {
                                List<String> targetTemplateIds = new ArrayList<>();
                                for (JsonNode targetId : targetTemplateIdsNode) {
                                    targetTemplateIds.add(targetId.asText());
                                }
                                config.targetTemplateIds = targetTemplateIds;
                            }
                        }
                        break;

                    // TEXT, LONG_TEXT, NUMBER, etc. không cần config
                }

                configs.add(config);
            }
        } catch (Exception e) {
            log.error("Error extracting child field configs from section field", e);
        }

        return configs;
    }

    /**
     * Extract table column configs từ TABLE field
     */
    private List<TableColumnConfig> extractTableColumnConfigs(FormField tableField) {
        List<TableColumnConfig> columnConfigs = new ArrayList<>();

        if (tableField.getTableConfig() == null) {
            return columnConfigs;
        }

        try {
            JsonNode tableConfigNode = tableField.getTableConfig();
            JsonNode columnsNode = tableConfigNode.get("columns");

            if (columnsNode != null && columnsNode.isArray()) {
                for (JsonNode columnNode : columnsNode) {
                    JsonNode typeNode = columnNode.get("type");
                    JsonNode keyNode = columnNode.get("key");

                    if (typeNode == null || keyNode == null) continue;

                    String columnType = typeNode.asText();
                    String columnKey = keyNode.asText();

                    TableColumnConfig config = new TableColumnConfig(columnKey, columnType);

                    // Extract config dựa trên column type
                    if ("INNOVATION_DATA".equals(columnType)) {
                        JsonNode innovationDataConfig = columnNode.get("innovationDataConfig");
                        if (innovationDataConfig != null) {
                            JsonNode sourceFieldKeyNode = innovationDataConfig.get("sourceFieldKey");
                            if (sourceFieldKeyNode != null) {
                                config.sourceFieldKey = sourceFieldKeyNode.asText();
                            }
                        }
                    } else if ("USER_DATA".equals(columnType)) {
                        JsonNode userDataConfig = columnNode.get("userDataConfig");
                        if (userDataConfig != null) {
                            JsonNode sourceFieldKeyNode = userDataConfig.get("sourceFieldKey");
                            if (sourceFieldKeyNode != null) {
                                config.sourceFieldKey = sourceFieldKeyNode.asText();
                            }
                        }
                    } else if ("REFERENCE".equals(columnType)) {
                        JsonNode referenceConfigNode = columnNode.get("referenceConfig");
                        if (referenceConfigNode != null) {
                            config.referenceConfig = referenceConfigNode;
                        }
                    } else if ("CONTRIBUTED".equals(columnType)) {
                        // CONTRIBUTED có contributionConfig với targetTemplateIds (mảng)
                        JsonNode contributionConfigNode = columnNode.get("contributionConfig");
                        if (contributionConfigNode != null) {
                            JsonNode targetTemplateIdsNode = contributionConfigNode.get("targetTemplateIds");
                            if (targetTemplateIdsNode != null && targetTemplateIdsNode.isArray()) {
                                List<String> targetTemplateIds = new ArrayList<>();
                                for (JsonNode targetId : targetTemplateIdsNode) {
                                    targetTemplateIds.add(targetId.asText());
                                }
                                config.targetTemplateIds = targetTemplateIds;
                            }
                        }
                    }

                    columnConfigs.add(config);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting table column configs", e);
        }

        return columnConfigs;
    }

    /**
     * Parse children fields từ JSON
     */
    private List<FormField> parseChildrenFields(JsonNode childrenNode) {
        List<FormField> childrenFields = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if (childrenNode.isArray()) {
                for (JsonNode childNode : childrenNode) {
                    FormField childField = objectMapper.treeToValue(childNode, FormField.class);
                    childrenFields.add(childField);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing children fields from JSON", e);
        }

        return childrenFields;
    }

    /**
     * Convert TemplateFieldResponse list sang Map<String, Object>
     * Key: label của field (hoặc fieldKey nếu có)
     * Value: value của field (đã convert từ JsonNode)
     */
    private Map<String, Object> convertTemplateFieldsToMap(List<TemplateFieldResponse> fields) {
        Map<String, Object> formDataMap = new HashMap<>();
        
        if (fields == null) {
            return formDataMap;
        }
        
        for (TemplateFieldResponse field : fields) {
            String fieldKey = field.getLabel(); // Dùng label làm key
            JsonNode valueNode = field.getValue();
            
            // Convert JsonNode sang Object
            Object value = null;
            if (valueNode != null && !valueNode.isNull()) {
                if (valueNode.isTextual()) {
                    value = valueNode.asText();
                } else if (valueNode.isNumber()) {
                    if (valueNode.isInt()) {
                        value = valueNode.asInt();
                    } else if (valueNode.isLong()) {
                        value = valueNode.asLong();
                    } else {
                        value = valueNode.asDouble();
                    }
                } else if (valueNode.isBoolean()) {
                    value = valueNode.asBoolean();
                } else if (valueNode.isArray()) {
                    // Array: convert sang List
                    List<Object> list = new ArrayList<>();
                    for (JsonNode item : valueNode) {
                        if (item.isTextual()) {
                            list.add(item.asText());
                        } else if (item.isObject()) {
                            // Object trong array: convert sang Map
                            Map<String, Object> itemMap = new HashMap<>();
                            item.fieldNames().forEachRemaining(fieldName -> {
                                itemMap.put(fieldName, convertJsonNodeToObject(item.get(fieldName)));
                            });
                            list.add(itemMap);
                        } else {
                            list.add(convertJsonNodeToObject(item));
                        }
                    }
                    value = list;
                } else if (valueNode.isObject()) {
                    // Object: convert sang Map
                    Map<String, Object> valueMap = new HashMap<>();
                    valueNode.fieldNames().forEachRemaining(fieldName -> {
                        valueMap.put(fieldName, convertJsonNodeToObject(valueNode.get(fieldName)));
                    });
                    value = valueMap;
                }
            }
            
            formDataMap.put(fieldKey, value);
        }
        
        return formDataMap;
    }
    
    /**
     * Helper: Convert JsonNode sang Object
     */
    private Object convertJsonNodeToObject(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isInt()) {
            return node.asInt();
        }
        if (node.isLong()) {
            return node.asLong();
        }
        if (node.isDouble()) {
            return node.asDouble();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        return node.toString();
    }

    /**
     * Helper classes
     */
    private static class FieldDataConfig {
        String fieldKey;
        FieldTypeEnum fieldType;
        boolean isRepeatable;
        List<ChildFieldConfig> childConfigs;
        List<TableColumnConfig> tableColumns;

        FieldDataConfig(String fieldKey, FieldTypeEnum fieldType) {
            this.fieldKey = fieldKey;
            this.fieldType = fieldType;
            this.childConfigs = new ArrayList<>();
            this.tableColumns = new ArrayList<>();
        }
    }

    private static class ChildFieldConfig {
        String fieldKey;
        String fieldType;
        String sourceFieldKey; // Cho INNOVATION_DATA và USER_DATA
        JsonNode referenceConfig; // Cho REFERENCE
        List<String> targetTemplateIds; // Cho CONTRIBUTED (thay vì formTemplateId)

        ChildFieldConfig(String fieldKey, String fieldType) {
            this.fieldKey = fieldKey;
            this.fieldType = fieldType;
        }
    }

    private static class TableColumnConfig {
        String columnKey;
        String columnType;
        String sourceFieldKey; // Cho INNOVATION_DATA và USER_DATA
        JsonNode referenceConfig; // Cho REFERENCE
        List<String> targetTemplateIds; // Cho CONTRIBUTED (thay vì formTemplateId)

        TableColumnConfig(String columnKey, String columnType) {
            this.columnKey = columnKey;
            this.columnType = columnType;
        }
    }
}
