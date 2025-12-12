package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.FieldTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.TemplateTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.EmbeddingResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SimilarInnovationWarning;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.FormDataRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InnovationEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(InnovationEmbeddingService.class);

    private final EmbeddingService embeddingService;
    private final InnovationRepository innovationRepository;
    private final FormDataRepository formDataRepository;

    @Value("${innovation.duplicate.threshold:0.70}")
    private double similarityThreshold;

    @Value("${innovation.embedding.metadata-table-threshold:0.5}")
    private double metadataTableThreshold;

    @Autowired
    public InnovationEmbeddingService(
            EmbeddingService embeddingService,
            InnovationRepository innovationRepository,
            FormDataRepository formDataRepository) {
        this.embeddingService = embeddingService;
        this.innovationRepository = innovationRepository;
        this.formDataRepository = formDataRepository;
    }

    /**
     * Flatten innovation data to text for embedding generation
     * Chỉ lấy innovationName và formData từ BAO_CAO_MO_TA template
     */
    public String flattenInnovationToText(Innovation innovation) {
        StringBuilder text = new StringBuilder();

        // 1. Chỉ thêm innovationName
        if (innovation.getInnovationName() != null && !innovation.getInnovationName().trim().isEmpty()) {
            text.append(innovation.getInnovationName().trim()).append(". ");
        }

        // 2. Lấy FormData
        List<FormData> formDataList = formDataRepository.findByInnovationIdWithRelations(innovation.getId());

        if (formDataList == null || formDataList.isEmpty()) {
            return text.toString();
        }

        // 3. CHỈ lấy FormData từ mẫu 2 (BAO_CAO_MO_TA)
        for (FormData formData : formDataList) {
            FormField field = formData.getFormField();
            JsonNode fieldValue = formData.getFieldValue();

            if (field == null || fieldValue == null || fieldValue.isNull()) {
                continue;
            }

            // Kiểm tra template type - CHỈ lấy BAO_CAO_MO_TA
            FormTemplate formTemplate = field.getFormTemplate();
            if (formTemplate == null || formTemplate.getTemplateType() != TemplateTypeEnum.BAO_CAO_MO_TA) {
                continue; // Bỏ qua các template khác
            }

            FieldTypeEnum fieldType = field.getFieldType();

            // Bỏ qua INNOVATION_DATA reference đến innovationName (đã có ở đầu)
            if (isDuplicateInnovationData(field)) {
                logger.debug("Bỏ qua INNOVATION_DATA reference đến '{}': {}",
                        getSourceFieldKey(field), field.getLabel());
                continue;
            }

            switch (fieldType) {
                case TEXT:
                case LONG_TEXT:
                    // Lấy text trực tiếp
                    String textValue = fieldValue.asText();
                    if (textValue != null && !textValue.trim().isEmpty()) {
                        // ✅ Thêm label của field + dấu ":" để có ngữ cảnh
                        String fieldLabel = field.getLabel();
                        if (fieldLabel != null && !fieldLabel.trim().isEmpty()) {
                            text.append(fieldLabel.trim()).append(": ");
                        }
                        // ✅ Dùng ". " (dấu chấm + cách) để ngắt câu
                        text.append(textValue.trim()).append(". ");
                    }
                    break;

                case TABLE:
                    // Kiểm tra xem có phải bảng metadata không
                    if (isMetadataTable(field)) {
                        logger.debug("Bỏ qua bảng metadata: {}", field.getLabel());
                        continue;
                    }

                    if (fieldValue.isArray()) {
                        for (JsonNode row : fieldValue) {
                            if (row.isObject()) {
                                JsonNode tableConfig = field.getTableConfig();
                                JsonNode columns = (tableConfig != null) ? tableConfig.get("columns") : null;

                                row.fields().forEachRemaining(entry -> {
                                    String columnKey = entry.getKey();
                                    String value = entry.getValue().asText();

                                    // Logic kiểm tra trùng lặp Innovation Data
                                    boolean isDuplicate = false;
                                    String columnLabel = ""; // Biến để lưu tên cột

                                    if (columns != null) {
                                        isDuplicate = isTableColumnDuplicateInnovationData(columns, columnKey);
                                        // ✅ Lấy label của cột từ columns dựa vào columnKey
                                        columnLabel = getColumnLabel(columns, columnKey);
                                    }

                                    if (!isDuplicate && value != null && !value.trim().isEmpty()) {
                                        // ✅ Thêm label của cột + dấu ":" để có ngữ cảnh: "Vật liệu: Sắt. "
                                        if (!columnLabel.isEmpty()) {
                                            text.append(columnLabel).append(": ");
                                        }
                                        // ✅ Dùng ". " (dấu chấm + cách) để ngắt câu
                                        text.append(value.trim()).append(". ");
                                    }
                                });
                            }
                        }
                    }
                    break;

                // Bỏ qua các field không có ý nghĩa ngữ nghĩa
                case NUMBER:
                case DATE:
                case SIGNATURE:
                case FILE:
                case INNOVATION_DATA:
                default:
                    break;
            }
        }

        return text.toString().trim();
    }

    /**
     * Kiểm tra xem field có phải là INNOVATION_DATA reference đến innovationName hoặc isScore không
     */
    private boolean isDuplicateInnovationData(FormField field) {
        if (field.getFieldType() != FieldTypeEnum.INNOVATION_DATA) {
            return false;
        }

        JsonNode innovationDataConfig = field.getInnovationDataConfig();
        if (innovationDataConfig == null || !innovationDataConfig.has("sourceFieldKey")) {
            return false;
        }

        String sourceFieldKey = innovationDataConfig.get("sourceFieldKey").asText();
        return "innovationName".equals(sourceFieldKey) || "isScore".equals(sourceFieldKey);
    }

    /**
     * Lấy sourceFieldKey từ innovationDataConfig
     */
    private String getSourceFieldKey(FormField field) {
        JsonNode innovationDataConfig = field.getInnovationDataConfig();
        if (innovationDataConfig != null && innovationDataConfig.has("sourceFieldKey")) {
            return innovationDataConfig.get("sourceFieldKey").asText();
        }
        return "";
    }

    /**
     * Kiểm tra xem cột trong table có phải là INNOVATION_DATA reference không
     */
    private boolean isTableColumnDuplicateInnovationData(JsonNode columns, String columnKey) {
        for (JsonNode column : columns) {
            if (column.has("key") && columnKey.equals(column.get("key").asText())) {
                JsonNode columnType = column.get("type");
                if (columnType != null && "INNOVATION_DATA".equals(columnType.asText())) {
                    JsonNode config = column.get("config");
                    if (config != null && config.has("sourceFieldKey")) {
                        String sourceFieldKey = config.get("sourceFieldKey").asText();
                        return "innovationName".equals(sourceFieldKey) || "isScore".equals(sourceFieldKey);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lấy label của cột từ tableConfig
     */
    private String getColumnLabel(JsonNode columns, String columnKey) {
        for (JsonNode column : columns) {
            if (column.has("key") && columnKey.equals(column.get("key").asText())) {
                if (column.has("label")) {
                    return column.get("label").asText();
                }
            }
        }
        return "";
    }

    /**
     * Kiểm tra xem table có phải là metadata table (ví dụ: bảng đồng tác giả) không
     * Dựa vào tỷ lệ USER_DATA columns trong table
     */
    private boolean isMetadataTable(FormField field) {
        JsonNode tableConfig = field.getTableConfig();
        if (tableConfig == null || !tableConfig.has("columns")) {
            return false;
        }

        JsonNode columns = tableConfig.get("columns");
        if (!columns.isArray() || columns.size() == 0) {
            return false;
        }

        int totalColumns = columns.size();
        int userDataColumns = 0;

        for (JsonNode column : columns) {
            JsonNode columnType = column.get("type");
            if (columnType != null && "USER_DATA".equals(columnType.asText())) {
                userDataColumns++;
            }
        }

        // Nếu tỷ lệ USER_DATA columns >= threshold thì coi là metadata table
        double ratio = (double) userDataColumns / totalColumns;
        return ratio >= metadataTableThreshold;
    }

    /**
     * Generate và lưu embedding cho innovation (async)
     */
    @Async
    public void saveEmbeddingAsync(String innovationId) {
        try {
            logger.info("Bắt đầu generate embedding cho innovation: {}", innovationId);

            // Retry logic: đợi transaction commit
            Innovation innovation = null;
            int maxRetries = 5;
            int retryDelay = 200; // 200ms
            
            for (int i = 0; i < maxRetries; i++) {
                try {
                    innovation = innovationRepository.findById(innovationId).orElse(null);
                    if (innovation != null) {
                        logger.debug("Tìm thấy innovation ở lần thử {}/{}", i + 1, maxRetries);
                        break;
                    }
                    if (i < maxRetries - 1) {
                        Thread.sleep(retryDelay);
                        logger.debug("Retry {}/{}: Đợi innovation được commit...", i + 1, maxRetries);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread bị interrupt khi đợi innovation commit");
                    return;
                } catch (Exception e) {
                    logger.warn("Lỗi khi tìm innovation (retry {}/{}): {}", i + 1, maxRetries, e.getMessage());
                    if (i < maxRetries - 1) {
                        try {
                            Thread.sleep(retryDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
            
            if (innovation == null) {
                logger.error("Không tìm thấy innovation sau {} lần thử: {}", maxRetries, innovationId);
                return;
            }

            // Flatten text
            String text = flattenInnovationToText(innovation);
            if (text == null || text.trim().isEmpty()) {
                logger.warn("Innovation {} không có text để generate embedding", innovationId);
                return;
            }

            logger.debug("Flattened text length: {}", text.length());

            // Generate embedding
            EmbeddingResponse embeddingResponse = embeddingService.generateEmbedding(text);
            List<Double> embedding = embeddingResponse.getEmbedding();

            // Convert to string format for PostgreSQL vector
            String embeddingString = "[" + embedding.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")) + "]";

            // Save to database
            innovationRepository.updateEmbedding(innovationId, embeddingString);

            logger.info("Đã lưu embedding cho innovation: {} (dimension: {})", 
                    innovationId, embeddingResponse.getDimension());

        } catch (Exception e) {
            logger.error("Lỗi khi generate và lưu embedding cho innovation {}: {}", 
                    innovationId, e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến flow chính
        }
    }

    /**
     * Tìm các innovation tương tự dựa trên embedding
     */
    @Transactional(readOnly = true)
    public List<SimilarInnovationWarning> findSimilarInnovations(String innovationId, double threshold) {
        try {
            Innovation innovation = innovationRepository.findById(innovationId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy innovation: " + innovationId));

            // Kiểm tra innovation có embedding chưa
            if (innovation.getEmbedding() == null || innovation.getEmbedding().trim().isEmpty()) {
                logger.warn("Innovation {} chưa có embedding", innovationId);
                return new ArrayList<>();
            }

            // Query similar innovations
            List<Object[]> results = innovationRepository.findSimilarInnovationsByEmbedding(
                    innovation.getEmbedding(),
                    innovationId,
                    threshold,
                    10 // Limit 10 results
            );

            // Map results to SimilarInnovationWarning
            List<SimilarInnovationWarning> warnings = new ArrayList<>();
            for (Object[] result : results) {
                Innovation similarInnovation = (Innovation) result[0];
                Double similarity = ((Number) result[1]).doubleValue();

                String riskLevel = determineRiskLevel(similarity);

                SimilarInnovationWarning warning = SimilarInnovationWarning.builder()
                        .innovationId(similarInnovation.getId())
                        .innovationName(similarInnovation.getInnovationName())
                        .authorName(similarInnovation.getUser() != null ? 
                                similarInnovation.getUser().getFullName() : "")
                        .departmentName(similarInnovation.getDepartment() != null ? 
                                similarInnovation.getDepartment().getDepartmentName() : "")
                        .status(similarInnovation.getStatus() != null ? 
                                similarInnovation.getStatus().name() : "")
                        .similarityScore(similarity)
                        .riskLevel(riskLevel)
                        .build();

                warnings.add(warning);
            }

            logger.info("Tìm thấy {} innovation tương tự cho innovation {}", warnings.size(), innovationId);
            return warnings;

        } catch (Exception e) {
            logger.error("Lỗi khi tìm similar innovations cho innovation {}: {}", 
                    innovationId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Tìm similar innovations với threshold mặc định
     */
    public List<SimilarInnovationWarning> findSimilarInnovations(String innovationId) {
        return findSimilarInnovations(innovationId, similarityThreshold);
    }

    /**
     * Xác định risk level dựa trên similarity score
     */
    private String determineRiskLevel(Double similarity) {
        if (similarity > 0.85) {
            return "HIGH";
        } else if (similarity >= 0.75) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}

