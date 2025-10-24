package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CreateTemplateWithFieldsRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateFormTemplateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.FieldDataRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CreateTemplateWithFieldsResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.FormTemplateResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.FormTemplateMapper;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormTemplateRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRoundRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final FormTemplateMapper formTemplateMapper;
    private final InnovationRoundRepository innovationRoundRepository;

    public FormTemplateService(FormTemplateRepository formTemplateRepository,
            InnovationRoundRepository innovationRoundRepository,
            FormTemplateMapper formTemplateMapper) {
        this.formTemplateRepository = formTemplateRepository;
        this.innovationRoundRepository = innovationRoundRepository;
        this.formTemplateMapper = formTemplateMapper;
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
                .orElseThrow(() -> new IdInvalidException("Không có innovation round hiện tại"));

        List<FormTemplate> templates = formTemplateRepository
                .findByInnovationRoundIdOrderByTemplateType(currentRound.getId());
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
            template.setTemplateContent(request.getTemplateContent());
        }

        // Upsert danh sách FormField nếu có
        if (request.getFields() != null) {
            processFormFields(template, request.getFields());
        }

        FormTemplate updatedTemplate = formTemplateRepository.save(template);
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
        template.setTemplateContent(request.getTemplateContent());
        template.setInnovationRound(innovationRound);

        FormTemplate savedTemplate = formTemplateRepository.save(template);

        // Sử dụng cùng logic xử lý fields như updateFormTemplate
        if (request.getFields() != null && !request.getFields().isEmpty()) {
            processFormFields(savedTemplate, request.getFields());
        }

        FormTemplate finalTemplate = formTemplateRepository.save(savedTemplate);
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

    // Helper method để xử lý fields cho cả create và update
    private void processFormFields(FormTemplate template, List<FieldDataRequest> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        // Map các field hiện có theo id để tiện cập nhật/xóa
        Map<String, FormField> existingById = template.getFormFields().stream()
                .filter(f -> f.getId() != null)
                .collect(java.util.stream.Collectors.toMap(FormField::getId, f -> f));

        Set<String> incomingIds = new java.util.HashSet<>();

        List<FormField> newList = new java.util.ArrayList<>();
        for (FieldDataRequest fd : fields) {
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
                // Tự sinh UUID cho các children nếu chưa có
                JsonNode processedChildren = generateChildrenIdsIfNeeded(fd.getChildren());
                entity.setChildren(processedChildren);
            }

            if (fd.getReferenceConfig() != null) {
                entity.setReferenceConfig(fd.getReferenceConfig());
            }

            if (fd.getUserDataConfig() != null) {
                entity.setUserDataConfig(fd.getUserDataConfig());
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

        // Xóa các field không còn được sử dụng
        template.getFormFields().removeAll(fieldsToRemove);

        // Thêm các field mới/cập nhật
        template.getFormFields().addAll(newList);
    }

    // 8. Tạo form template không gắn round cụ thể (template chung)
    @Transactional
    public CreateTemplateResponse createTemplate(CreateTemplateRequest request) {
        FormTemplate template = new FormTemplate();
        template.setTemplateType(request.getTemplateType());
        template.setTargetRole(request.getTargetRole());
        template.setTemplateContent(request.getTemplateContent());

        if (request.getRoundId() != null && !request.getRoundId().trim().isEmpty()) {
            InnovationRound innovationRound = innovationRoundRepository.findById(request.getRoundId().trim())
                    .orElseThrow(() -> new IdInvalidException(
                            "Innovation round không tồn tại với ID: " + request.getRoundId()));
            template.setInnovationRound(innovationRound);
        } else {
            template.setInnovationRound(null);
        }

        FormTemplate savedTemplate = formTemplateRepository.save(template);

        // Sử dụng cùng logic xử lý fields như các method khác
        if (request.getFields() != null && !request.getFields().isEmpty()) {
            processFormFields(savedTemplate, request.getFields());
        }

        FormTemplate finalTemplate = formTemplateRepository.save(savedTemplate);
        return formTemplateMapper.toCreateTemplateResponse(finalTemplate);
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

    /**
     * Tự sinh UUID cho các column trong JsonNode tableConfig nếu chưa có ID
     */
    private JsonNode generateColumnIdsIfNeeded(JsonNode tableConfig) {
        if (tableConfig != null && tableConfig.has("columns")) {
            try {
                ObjectNode tableConfigNode = (ObjectNode) tableConfig.deepCopy();
                ArrayNode columnsNode = (ArrayNode) tableConfigNode.get("columns");

                for (int i = 0; i < columnsNode.size(); i++) {
                    ObjectNode columnNode = (ObjectNode) columnsNode.get(i);
                    if (!columnNode.has("id") || columnNode.get("id").isNull() ||
                            columnNode.get("id").asText().trim().isEmpty()) {
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
     * Tự sinh UUID cho các children trong JsonNode children nếu chưa có ID
     */
    private JsonNode generateChildrenIdsIfNeeded(JsonNode children) {
        if (children != null && children.isArray()) {
            try {
                ArrayNode childrenNode = (ArrayNode) children.deepCopy();

                for (int i = 0; i < childrenNode.size(); i++) {
                    ObjectNode childNode = (ObjectNode) childrenNode.get(i);
                    if (!childNode.has("id") || childNode.get("id").isNull() ||
                            childNode.get("id").asText().trim().isEmpty()) {
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
}
