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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TargetRoleCode;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.ReportStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Report;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReportRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DigitalSignatureRepository;
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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TemplateFieldResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.NotFoundException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormFieldRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.ReviewScoreRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ScoreCriteriaDetail;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.HtmlTemplateUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final UserService userService;
    private final CouncilService councilService;
    private final InnovationService innovationService;
    private final CouncilRepository councilRepository;
    private final InnovationRepository innovationRepository;
    private final FormFieldRepository formFieldRepository;
    private final FormDataRepository formDataRepository;
    private final ReviewScoreRepository reviewScoreRepository;
    private final ReportRepository reportRepository;
    private final DigitalSignatureRepository digitalSignatureRepository;
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
            FormFieldRepository formFieldRepository,
            FormDataRepository formDataRepository,
            ReviewScoreRepository reviewScoreRepository,
            ReportRepository reportRepository,
            DigitalSignatureRepository digitalSignatureRepository,
            ObjectMapper objectMapper) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationRoundRepository = innovationRoundRepository;
        this.formTemplateMapper = formTemplateMapper;
        this.userService = userService;
        this.councilService = councilService;
        this.innovationService = innovationService;
        this.councilRepository = councilRepository;
        this.innovationRepository = innovationRepository;
        this.formFieldRepository = formFieldRepository;
        this.formDataRepository = formDataRepository;
        this.reviewScoreRepository = reviewScoreRepository;
        this.reportRepository = reportRepository;
        this.digitalSignatureRepository = digitalSignatureRepository;
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

        // ✅ Không cần update HTML IDs nữa - chỉ dùng data-field-key
        // updateHtmlTemplateContent(updatedTemplate);
        // updatedTemplate = formTemplateRepository.save(updatedTemplate);

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

        // ✅ Không cần update HTML IDs nữa - chỉ dùng data-field-key
        // updateHtmlTemplateContent(finalTemplate);
        // finalTemplate = formTemplateRepository.save(finalTemplate);

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

            // ✅ Không cần update HTML IDs nữa - chỉ dùng data-field-key
            // updateHtmlTemplateContent(finalTemplate);
            // finalTemplate = formTemplateRepository.save(finalTemplate);
            // entityManager.flush();

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
            throw new IdInvalidException(
                    "Template type phải là BIEN_BAN_HOP, TONG_HOP_DE_NGHI, hoặc TONG_HOP_CHAM_DIEM");
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
        CreateTemplateWithFieldsResponse templateResponse = formTemplateMapper
                .toCreateTemplateWithFieldsResponse(template);

        // 7. Build field data map động
        Map<String, Object> fieldDataMap = buildFieldDataMapDynamic(template, councilId, isScoreFilter);

        // 7.5. Check và merge Report draft nếu có (chưa ký)
        mergeDraftReportData(fieldDataMap, templateType, councilId);

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
     * Merge draft Report data vào fieldDataMap nếu có (chưa ký)
     */
    private void mergeDraftReportData(
            Map<String, Object> fieldDataMap,
            TemplateTypeEnum templateType,
            String councilId) {
        try {
            // 1. Map TemplateTypeEnum -> DocumentTypeEnum
            DocumentTypeEnum documentType = mapTemplateTypeToDocumentType(templateType);
            if (documentType == null) {
                return; // Không phải template type cần check
            }

            // 2. Lấy current user và departmentId
            User currentUser = userService.getCurrentUser();
            if (currentUser == null || currentUser.getDepartment() == null) {
                return;
            }
            String departmentId = currentUser.getDepartment().getId();

            // 3. Tìm Report draft (chưa có DigitalSignature của thư ký)
            Optional<Report> draftReportOpt = reportRepository
                    .findByDepartmentIdAndReportType(departmentId, documentType);

            if (draftReportOpt.isEmpty()) {
                return; // Không có draft
            }

            Report report = draftReportOpt.get();

            // 4. Merge reportData vào fieldDataMap (không phân biệt status - để hiển thị)
            if (report.getReportData() != null && !report.getReportData().isEmpty()) {
                Map<String, Object> draftData = report.getReportData();
                // Merge: draft data override council results data
                draftData.forEach((key, value) -> {
                    if (value != null) {
                        fieldDataMap.put(key, value);
                    }
                });
                ReportStatusEnum reportStatus = report.getStatus();
                if (reportStatus == null) {
                    reportStatus = ReportStatusEnum.DRAFT; // Default nếu null
                }
                log.info("Đã merge Report data cho template type: {}, departmentId: {}, status: {}", 
                        templateType, departmentId, reportStatus);
            }
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không ảnh hưởng đến flow chính
            log.warn("Lỗi khi merge draft Report data: {}", e.getMessage());
        }
    }

    /**
     * Map TemplateTypeEnum sang DocumentTypeEnum
     */
    private DocumentTypeEnum mapTemplateTypeToDocumentType(TemplateTypeEnum templateType) {
        if (templateType == null) {
            return null;
        }
        switch (templateType) {
            case BIEN_BAN_HOP:
                return DocumentTypeEnum.REPORT_MAU_3;
            case TONG_HOP_DE_NGHI:
                return DocumentTypeEnum.REPORT_MAU_4;
            case TONG_HOP_CHAM_DIEM:
                return DocumentTypeEnum.REPORT_MAU_5;
            default:
                return null;
        }
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

            // Build data cho từng field - TRUYỀN templateId
            String templateId = template.getId();
            for (FieldDataConfig fieldConfig : fieldsNeedingData) {
                Object fieldData = buildDataForField(fieldConfig, filteredResults, councilId, templateId);
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
                boolean hasDataFields = childConfigs.stream()
                        .anyMatch(config -> config.fieldType.equals("INNOVATION_DATA") ||
                                config.fieldType.equals("USER_DATA") ||
                                config.fieldType.equals("REFERENCE") ||
                                config.fieldType.equals("CONTRIBUTED"));

                if (hasDataFields) {
                    FieldDataConfig fieldConfig = new FieldDataConfig(field.getFieldKey(), FieldTypeEnum.SECTION);
                    // isRepeatable không được sử dụng, bỏ qua
                    fieldConfig.childConfigs = childConfigs;
                    fieldsNeedingData.add(fieldConfig);
                }
            }
            // TABLE field: Kiểm tra columns có data columns không
            else if (fieldType == FieldTypeEnum.TABLE && field.getTableConfig() != null) {
                List<TableColumnConfig> tableColumns = extractTableColumnConfigs(field);
                boolean hasDataColumns = tableColumns.stream()
                        .anyMatch(config -> config.columnType.equals("INNOVATION_DATA") ||
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
     * Helper method để convert value sang Integer
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Map columnKey sang criteriaId cho các cột điểm
     */
    private String getCriteriaIdFromColumnKey(String columnKey) {
        if (columnKey == null) {
            return null;
        }
        
        // Map các columnKey điểm với criteriaId cố định
        switch (columnKey) {
            case "tinh_moi_cua_sang_kien":
                return "1";
            case "tinh_kha_thi_cua_sang_kien":
                return "2";
            case "tinh_hieu_qua_cua_sang_kien":
                return "3";
            default:
                return null;
        }
    }

    /**
     * Build data cho một field cụ thể
     */
    private Object buildDataForField(
            FieldDataConfig fieldConfig,
            List<InnovationResultDetail> filteredResults,
            String councilId,
            String templateId) {

        switch (fieldConfig.fieldType) {
            case SECTION:
                // Build array of instance data
                List<Map<String, Object>> instanceDataList = new ArrayList<>();

                for (InnovationResultDetail result : filteredResults) {
                    Map<String, Object> instanceData = new HashMap<>();

                    // Resolve từng child field - TRUYỀN templateId
                    for (ChildFieldConfig childConfig : fieldConfig.childConfigs) {
                        Object value = resolveChildFieldValue(childConfig, result, councilId, templateId);
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

                    // Resolve từng table column - TRUYỀN templateId
                    for (TableColumnConfig columnConfig : fieldConfig.tableColumns) {
                        Object value = resolveTableColumnValue(columnConfig, result, councilId, templateId);
                        // Luôn put vào map, kể cả null
                        rowData.put(columnConfig.columnKey, value);
                    }

                    // Tính tong_diem nếu có các cột điểm
                    Integer tinhMoi = getIntegerValue(rowData.get("tinh_moi_cua_sang_kien"));
                    Integer tinhKhaThi = getIntegerValue(rowData.get("tinh_kha_thi_cua_sang_kien"));
                    Integer tinhHieuQua = getIntegerValue(rowData.get("tinh_hieu_qua_cua_sang_kien"));
                    
                    if (tinhMoi != null || tinhKhaThi != null || tinhHieuQua != null) {
                        // Tính tổng điểm nếu có ít nhất 1 điểm (số nguyên)
                        int sum = (tinhMoi != null ? tinhMoi : 0) +
                                 (tinhKhaThi != null ? tinhKhaThi : 0) +
                                 (tinhHieuQua != null ? tinhHieuQua : 0);
                        rowData.put("tong_diem", sum);
                    } else {
                        // Nếu không có điểm nào, dùng averageScore từ result (convert sang Integer nếu cần)
                        if (result.getAverageScore() != null) {
                            rowData.put("tong_diem", result.getAverageScore().intValue());
                        } else {
                            rowData.put("tong_diem", null);
                        }
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
            String councilId,
            String templateId) {

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
                // Lấy từ database
                return getContributedFieldValue(childConfig.fieldKey, result.getInnovationId(), templateId);

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
            String councilId,
            String templateId) {

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
                // Lấy từ database
                return getContributedFieldValue(columnConfig.columnKey, result.getInnovationId(), templateId);

            case "NUMBER":
                // Kiểm tra xem có phải là cột điểm không
                String criteriaId = getCriteriaIdFromColumnKey(columnConfig.columnKey);
                if (criteriaId != null) {
                    return getScoreValue(result.getInnovationId(), criteriaId);
                }
                // Nếu không phải cột điểm, trả về null (NUMBER thường không có data động)
                return null;

            default:
                return null;
        }

        return null;
    }

    /**
     * Lấy điểm trung bình cho criteria cụ thể từ ReviewScore
     * Tính trung bình điểm của tất cả reviewers cho criteria này
     * Trả về Integer (số nguyên)
     */
    private Integer getScoreValue(String innovationId, String criteriaId) {
        try {
            // Lấy tất cả ReviewScore cho innovation này
            List<ReviewScore> reviewScores = reviewScoreRepository.findByInnovationId(innovationId);
            
            if (reviewScores == null || reviewScores.isEmpty()) {
                log.warn("No review scores found for innovationId: " + innovationId);
                return null;
            }

            List<Integer> scores = new ArrayList<>();
            
            // Parse scoringDetails từ mỗi ReviewScore
            for (ReviewScore reviewScore : reviewScores) {
                if (reviewScore.getScoringDetails() == null || reviewScore.getScoringDetails().isNull()) {
                    continue;
                }

                try {
                    // Convert JsonNode sang List<ScoreCriteriaDetail>
                    List<ScoreCriteriaDetail> scoringDetails = objectMapper.convertValue(
                            reviewScore.getScoringDetails(),
                            objectMapper.getTypeFactory().constructCollectionType(
                                    List.class, ScoreCriteriaDetail.class));

                    // Tìm điểm cho criteriaId này
                    for (ScoreCriteriaDetail detail : scoringDetails) {
                        if (criteriaId.equals(detail.getCriteriaId()) && detail.getScore() != null) {
                            scores.add(detail.getScore());
                            break; // Mỗi reviewer chỉ có 1 điểm cho mỗi criteria
                        }
                    }
                } catch (Exception e) {
                    log.error("Error parsing scoringDetails for reviewScore: " + reviewScore.getId(), e);
                }
            }

            // Tính trung bình (làm tròn)
            if (scores.isEmpty()) {
                return null;
            }

            int sum = scores.stream().mapToInt(Integer::intValue).sum();
            return Math.round((float) sum / scores.size());

        } catch (Exception e) {
            log.error("Error getting score value for innovationId: " + innovationId + ", criteriaId: " + criteriaId, e);
            return null;
        }
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
            case "basisText":
                // Lấy từ Innovation entity
                try {
                    Innovation innovation = innovationRepository.findById(result.getInnovationId())
                            .orElse(null);
                    if (innovation != null) {
                        return innovation.getBasisText();
                    }
                } catch (Exception e) {
                    log.error("Error getting basisText for innovation: " + result.getInnovationId(), e);
                }
                return null;
            case "ketLuanVeSangKien":
                if (result.getFinalDecision() == null) {
                    return null; // Chưa có quyết định
                }
                return result.getFinalDecision() 
                    ? "Đề nghị Hội đồng Sáng kiến Trường xét công nhận" 
                    : "Không đề nghị Hội đồng Sáng kiến Trường xét công nhận";
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
            InnovationFormDataResponse innovationFormData = innovationService
                    .getInnovationWithFormDataById(innovationId);

            if (innovationFormData == null || innovationFormData.getTemplates() == null) {
                return "[Không tìm thấy dữ liệu từ " + sourceTemplateId + "]";
            }

            // Tìm template data tương ứng với sourceTemplateId
            Map<String, Object> sourceData = null;
            for (var template : innovationFormData.getTemplates()) {
                if (template.getTemplateId().equals(sourceTemplateId)) {
                    // Convert fields sang Map<String, Object> với fieldKey làm key
                    sourceData = convertTemplateFieldsToMap(template.getFields(), sourceTemplateId);
                    break;
                }
            }

            if (sourceData == null) {
                return "[Không tìm thấy dữ liệu từ " + sourceTemplateId + "]";
            }

            // Format dữ liệu
            if (displayFormat != null && !displayFormat.isEmpty()) {
                // Kiểm tra xem có field nào từ table không (có chứa ".")
                boolean hasTableFields = sourceFieldKeys.stream().anyMatch(key -> key.contains("."));
                
                if (hasTableFields) {
                    // Lấy table data từ field đầu tiên có table
                    String firstTableField = sourceFieldKeys.stream()
                        .filter(key -> key.contains("."))
                        .findFirst()
                        .orElse(null);
                    
                    if (firstTableField != null) {
                        String[] parts = firstTableField.split("\\.", 2);
                        String tableKey = parts[0];
                        Object tableData = sourceData.get(tableKey);
                        
                        if (tableData instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> tableRows = (List<Map<String, Object>>) tableData;
                            
                            // Format từng row
                            List<String> formattedRows = new ArrayList<>();
                            for (Map<String, Object> row : tableRows) {
                                String formattedRow = displayFormat;
                                for (String fieldKey : sourceFieldKeys) {
                                    String value = "";
                                    
                                    if (fieldKey.contains(".")) {
                                        // Lấy từ row hiện tại (table column)
                                        String[] fieldParts = fieldKey.split("\\.", 2);
                                        String columnKey = fieldParts[1];
                                        Object columnValue = row.get(columnKey);
                                        
                                        // Nếu không tìm thấy bằng columnKey, thử fuzzy match
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
                                        
                                        value = columnValue != null ? columnValue.toString() : "";
                                    } else {
                                        // Lấy từ sourceData (field thường, không phải table)
                                        Object fieldValue = sourceData.get(fieldKey);
                                        if (fieldValue == null) {
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
                                            fieldValue = similarKey != null ? sourceData.get(similarKey) : null;
                                        }
                                        value = fieldValue != null ? fieldValue.toString() : "";
                                    }
                                    
                                    // Thay thế trong format
                                    String keyForFormat = fieldKey.contains(".") 
                                        ? fieldKey.substring(fieldKey.lastIndexOf(".") + 1)
                                        : fieldKey;
                                    formattedRow = formattedRow.replace("{" + keyForFormat + "}", value);
                                    formattedRow = formattedRow.replace("{" + fieldKey + "}", value);
                                }
                                formattedRows.add(formattedRow);
                            }
                            
                            // Join các rows bằng multiRowSeparator
                            String separator = (multiRowSeparator != null && !multiRowSeparator.isEmpty()) 
                                ? multiRowSeparator : "\n";
                            return String.join(separator, formattedRows);
                        }
                    }
                }
                
                // Fallback về logic cũ nếu không phải table fields hoặc không có displayFormat
                // Lấy giá trị từ các field (hỗ trợ nested path cho TABLE)
                List<String> values = new ArrayList<>();
                for (String fieldKey : sourceFieldKeys) {
                    String value = getNestedFieldValue(sourceData, fieldKey, multiRowSeparator);
                    values.add(value != null ? value : "");
                }
                
                if (values.stream().allMatch(v -> v.trim().isEmpty())) {
                    return "";
                }
                
                String result = displayFormat;
                for (int i = 0; i < sourceFieldKeys.size(); i++) {
                    // Trong displayFormat, có thể dùng {ho_va_ten} thay vì
                    // {danh_sach_tac_gia.ho_va_ten}
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

            // Không có displayFormat, join các values bằng multiRowSeparator
            List<String> values = new ArrayList<>();
            for (String fieldKey : sourceFieldKeys) {
                String value = getNestedFieldValue(sourceData, fieldKey, multiRowSeparator);
                values.add(value != null ? value : "");
            }
            
            if (values.stream().allMatch(v -> v.trim().isEmpty())) {
                return "";
            }

            return String.join(multiRowSeparator, values);

        } catch (Exception e) {
            log.error("Error resolving REFERENCE field value for innovation: " + innovationId, e);
            return "[Lỗi khi lấy dữ liệu tham chiếu]";
        }
    }

    /**
     * Lấy giá trị từ sourceData, hỗ trợ nested path (tableKey.columnKey)
     * 
     * @param sourceData        Map từ convertTemplateFieldsToMap (key là fieldKey)
     * @param fieldKey          Có thể là "fieldKey" hoặc "tableKey.columnKey"
     *                          (fieldKey của TABLE field)
     * @param multiRowSeparator Separator để join các rows trong table (thường là
     *                          "\n")
     */
    private String getNestedFieldValue(Map<String, Object> sourceData, String fieldKey, String multiRowSeparator) {
        // Kiểm tra xem có phải nested path không (table.column)
        if (fieldKey.contains(".")) {
            String[] parts = fieldKey.split("\\.", 2);
            String tableKey = parts[0]; // fieldKey của TABLE field (ví dụ: "danh_sach_tac_gia")
            String columnKey = parts[1]; // column key trong table (ví dụ: "ho_va_ten")

            // Lấy table data (key là fieldKey)
            Object tableData = sourceData.get(tableKey);

            if (tableData == null) {
                log.warn("Không tìm thấy table data với fieldKey: " + tableKey);
                return null;
            }

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

                // Join các giá trị bằng multiRowSeparator
                String separator = (multiRowSeparator != null && !multiRowSeparator.isEmpty()) ? multiRowSeparator
                        : "\n";
                return String.join(separator, columnValues);
            }

            log.warn("Table field '" + tableKey + "' không phải là List (TABLE field)");
            return null;
        }

        // Nếu không phải nested path, lấy giá trị trực tiếp (key là fieldKey)
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
 * Lấy CONTRIBUTED field value từ database
 * CONTRIBUTED field đã có sẵn data trong formData table
 * Đối với CONTRIBUTED field trong SECTION/TABLE, fieldValue có structure:
 * {"value": "...", "fieldKey": "..."}
 * Cần tìm fieldKey trong fieldValue.fieldKey, không phải formField.fieldKey
 */
private Object getContributedFieldValue(String fieldKey, String innovationId, String templateId) {
    log.info("=== getContributedFieldValue START ===");
    log.info("fieldKey: " + fieldKey);
    log.info("innovationId: " + innovationId);
    log.info("templateId: " + templateId);
    
    try {
        // Lấy tất cả formData của innovation (không filter theo template)
        List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovationId);
        
        log.info("formDataList size: " + (formDataList != null ? formDataList.size() : 0));
        
        if (formDataList == null || formDataList.isEmpty()) {
            log.warn("formDataList is empty for innovationId: " + innovationId);
            return null;
        }

        // Tìm FormData có fieldValue.fieldKey trùng với fieldKey
        // (Đối với CONTRIBUTED trong SECTION/TABLE, fieldKey nằm trong fieldValue object)
        for (FormData formData : formDataList) {
            if (formData.getFieldValue() != null) {
                JsonNode fieldValueNode = formData.getFieldValue();
                
                // Kiểm tra nếu fieldValue là object và có property "fieldKey"
                // Đây là trường hợp CONTRIBUTED field trong SECTION/TABLE
                if (fieldValueNode.isObject() && fieldValueNode.has("fieldKey")) {
                    String fieldKeyInValue = fieldValueNode.get("fieldKey").asText();
                    log.info("Checking fieldValue.fieldKey='" + fieldKeyInValue + "' with targetFieldKey='" + fieldKey + "'");
                    
                    if (fieldKeyInValue != null && fieldKeyInValue.equals(fieldKey)) {
                        log.info("FOUND matching fieldKey in fieldValue!");
                        
                        // Lấy value từ fieldValue object
                        if (fieldValueNode.has("value")) {
                            JsonNode valueNode = fieldValueNode.get("value");
                            Object result = convertJsonNodeToObject(valueNode);
                            log.info("Returning value from fieldValue: " + result);
                            return result;
                        } else {
                            log.warn("fieldValue has fieldKey but no value property");
                        }
                    }
                } else {
                    // Trường hợp field thường: kiểm tra formField.fieldKey
                    // (Nhưng CONTRIBUTED thường nằm trong SECTION/TABLE nên ít khi vào đây)
                    if (formData.getFormField() != null && 
                        formData.getFormField().getFieldKey() != null &&
                        formData.getFormField().getFieldKey().equals(fieldKey)) {
                        
                        log.info("FOUND matching fieldKey in formField (direct field)");
                        Object result = convertJsonNodeToObject(fieldValueNode);
                        log.info("Returning value (direct): " + result);
                        return result;
                    }
                }
            }
        }
        
        log.warn("No matching fieldKey found for: " + fieldKey);

    } catch (Exception e) {
        log.error("Error getting CONTRIBUTED field value from database for innovation: " + innovationId + 
                  ", fieldKey: " + fieldKey, e);
        e.printStackTrace();
    }
    
    log.info("=== getContributedFieldValue END (returning null) ===");
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

                if (typeNode == null || fieldKeyNode == null)
                    continue;

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

                    if (typeNode == null || keyNode == null)
                        continue;

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
     * Key: fieldKey (fetch từ FormField trong database)
     * Value: value của field (đã convert từ JsonNode)
     */
    private Map<String, Object> convertTemplateFieldsToMap(List<TemplateFieldResponse> fields, String templateId) {
        Map<String, Object> formDataMap = new HashMap<>();

        if (fields == null || templateId == null) {
            return formDataMap;
        }

        // Fetch FormFields từ database để lấy fieldKey
        List<FormField> formFields = formFieldRepository.findByTemplateId(templateId);
        Map<String, String> labelToFieldKeyMap = new HashMap<>();
        for (FormField formField : formFields) {
            if (formField.getLabel() != null && formField.getFieldKey() != null) {
                labelToFieldKeyMap.put(formField.getLabel(), formField.getFieldKey());
            }
        }

        for (TemplateFieldResponse field : fields) {
            // Lấy fieldKey từ map (dựa trên label)
            String fieldKey = labelToFieldKeyMap.get(field.getLabel());
            if (fieldKey == null) {
                // Fallback: dùng label nếu không tìm thấy fieldKey
                fieldKey = field.getLabel();
                log.warn("Không tìm thấy fieldKey cho label: " + field.getLabel() + " trong template: " + templateId);
            }

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

}
