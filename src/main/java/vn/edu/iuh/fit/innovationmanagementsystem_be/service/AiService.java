package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.AiAnalysisResult;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AiAnalysisResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.AiAnalysisResultRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ai.AiProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final AiProvider aiProvider;
    private final InnovationRepository innovationRepository;
    private final AiAnalysisCacheService cacheService;
    private final AiAnalysisResultRepository analysisResultRepository;
    private final ObjectMapper objectMapper;

    public AiService(AiProvider aiProvider, InnovationRepository innovationRepository,
            AiAnalysisCacheService cacheService, AiAnalysisResultRepository analysisResultRepository,
            ObjectMapper objectMapper) {
        this.aiProvider = aiProvider;
        this.innovationRepository = innovationRepository;
        this.cacheService = cacheService;
        this.analysisResultRepository = analysisResultRepository;
        this.objectMapper = objectMapper;
        logger.info("AiService initialized with provider: {}", aiProvider.getProviderName());
    }

    public AiAnalysisResponse analyzeInnovation(String innovationId) {
        Innovation innovation = findInnovationById(innovationId);
        String content = extractInnovationContent(innovation);

        if (content == null || content.trim().isEmpty()) {
            throw new IdInvalidException("Sáng kiến không có nội dung để phân tích");
        }

        String contentHash = cacheService.generateContentHash(content);

        // 1. Kiểm tra trong database trước
        Optional<AiAnalysisResult> dbResult = analysisResultRepository.findByInnovationIdAndContentHash(innovationId,
                contentHash);
        if (dbResult.isPresent()) {
            logger.info("Trả về kết quả từ DATABASE cho innovation: {}", innovationId);
            return convertToResponse(dbResult.get());
        }

        // 2. Kiểm tra Redis cache
        AiAnalysisResponse cachedResponse = cacheService.getCachedAnalysis(innovationId, contentHash);
        if (cachedResponse != null) {
            logger.info("Trả về kết quả từ REDIS CACHE cho innovation: {}", innovationId);
            return cachedResponse;
        }

        // 3. Gọi Ollama và lưu vào database + cache
        logger.info("Không tìm thấy trong DB/Cache, gọi Ollama để phân tích innovation: {}", innovationId);
        logger.info("Analyzing innovation: {} using provider: {}", innovation.getInnovationName(),
                aiProvider.getProviderName());
        AiAnalysisResponse response = aiProvider.analyze(innovationId, innovation.getInnovationName(), content);

        // Lưu vào database
        saveToDatabase(innovationId, innovation.getInnovationName(), contentHash, response);

        // Lưu vào Redis cache
        cacheService.cacheAnalysis(innovationId, contentHash, response);

        return response;
    }

    @Transactional
    public void preComputeAnalysis(String innovationId) {
        try {
            logger.info("[PRE-COMPUTE] Bắt đầu pre-compute analysis cho innovation: {}", innovationId);

            Innovation innovation = findInnovationById(innovationId);
            String content = extractInnovationContent(innovation);

            if (content == null || content.trim().isEmpty()) {
                logger.warn("[PRE-COMPUTE] Innovation {} không có nội dung để pre-compute analysis", innovationId);
                return;
            }

            String contentHash = cacheService.generateContentHash(content);

            // Kiểm tra đã có trong database chưa
            if (analysisResultRepository.existsByInnovationIdAndContentHash(innovationId, contentHash)) {
                logger.info("[PRE-COMPUTE] Đã có kết quả trong DATABASE cho innovation: {}, bỏ qua pre-compute",
                        innovationId);
                return;
            }

            logger.info("[PRE-COMPUTE] Đang gọi Ollama để phân tích innovation: {} ...", innovationId);
            long startTime = System.currentTimeMillis();

            AiAnalysisResponse response = aiProvider.analyze(innovationId, innovation.getInnovationName(), content);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("[PRE-COMPUTE] Ollama phân tích xong trong {}ms cho innovation: {}", duration, innovationId);

            // Lưu vào database
            saveToDatabase(innovationId, innovation.getInnovationName(), contentHash, response);
            logger.info("[PRE-COMPUTE] Đã lưu kết quả vào DATABASE cho innovation: {}", innovationId);

            // Lưu vào Redis cache
            cacheService.cacheAnalysis(innovationId, contentHash, response);
            logger.info("[PRE-COMPUTE] Đã lưu kết quả vào REDIS CACHE cho innovation: {}", innovationId);

            logger.info("[PRE-COMPUTE] Hoàn thành pre-compute analysis cho innovation: {}", innovationId);
        } catch (Exception e) {
            logger.error("[PRE-COMPUTE] Lỗi khi pre-compute analysis cho innovation: {} - {}", innovationId,
                    e.getMessage(), e);
        }
    }

    private void saveToDatabase(String innovationId, String innovationName, String contentHash,
            AiAnalysisResponse response) {
        try {
            // Serialize toàn bộ response thành JSON
            String analysisJson = objectMapper.writeValueAsString(response);

            AiAnalysisResult result = AiAnalysisResult.builder()
                    .innovationId(innovationId)
                    .innovationName(innovationName)
                    .contentHash(contentHash)
                    .analysisJson(analysisJson)
                    .generatedAt(response.getGeneratedAt() != null ? response.getGeneratedAt() : LocalDateTime.now())
                    .build();

            analysisResultRepository.save(result);
            logger.info("Đã lưu AiAnalysisResult vào database cho innovation: {}", innovationId);
        } catch (JsonProcessingException e) {
            logger.error("Lỗi khi serialize JSON cho innovation: {} - {}", innovationId, e.getMessage());
        }
    }

    private AiAnalysisResponse convertToResponse(AiAnalysisResult result) {
        try {
            String analysisJson = result.getAnalysisJson();

            // Parse từ analysisJson
            if (analysisJson != null && !analysisJson.isEmpty()) {
                // Fix JSON bị cắt (thiếu dấu } cuối)
                String fixedJson = fixTruncatedJson(analysisJson);
                AiAnalysisResponse response = objectMapper.readValue(fixedJson, AiAnalysisResponse.class);

                // Nếu các field rỗng nhưng analysis có JSON, parse lại từ analysis
                if ((response.getSummary() == null || response.getSummary().isEmpty())
                        && response.getAnalysis() != null
                        && response.getAnalysis().trim().startsWith("{")) {
                    logger.info("Các field rỗng, đang parse lại từ analysis field");
                    String innerJson = fixTruncatedJson(response.getAnalysis());
                    JsonNode jsonNode = objectMapper.readTree(innerJson);

                    String summary = getTextFromJson(jsonNode, "summary", "");
                    String analysis = getTextFromJson(jsonNode, "analysis", "");
                    List<String> keyPoints = parseJsonArray(jsonNode.get("keyPoints"));
                    List<String> strengths = parseJsonArray(jsonNode.get("strengths"));
                    List<String> weaknesses = parseJsonArray(jsonNode.get("weaknesses"));
                    List<String> suggestions = parseJsonArray(jsonNode.get("suggestions"));

                    return AiAnalysisResponse.builder()
                            .innovationId(response.getInnovationId())
                            .innovationName(response.getInnovationName())
                            .summary(summary)
                            .keyPoints(keyPoints)
                            .strengths(strengths)
                            .weaknesses(weaknesses)
                            .suggestions(suggestions)
                            .analysis(analysis)
                            .generatedAt(response.getGeneratedAt())
                            .build();
                }

                return response;
            }

            // Fallback nếu không có dữ liệu
            return AiAnalysisResponse.builder()
                    .innovationId(result.getInnovationId())
                    .innovationName(result.getInnovationName())
                    .summary("")
                    .keyPoints(new ArrayList<>())
                    .strengths(new ArrayList<>())
                    .weaknesses(new ArrayList<>())
                    .suggestions(new ArrayList<>())
                    .analysis("")
                    .generatedAt(result.getGeneratedAt())
                    .build();
        } catch (Exception e) {
            logger.error("Lỗi khi convert AiAnalysisResult sang Response: {}", e.getMessage());
            return AiAnalysisResponse.builder()
                    .innovationId(result.getInnovationId())
                    .innovationName(result.getInnovationName())
                    .summary("")
                    .keyPoints(new ArrayList<>())
                    .strengths(new ArrayList<>())
                    .weaknesses(new ArrayList<>())
                    .suggestions(new ArrayList<>())
                    .analysis(result.getAnalysisJson())
                    .generatedAt(result.getGeneratedAt())
                    .build();
        }
    }

    private String getTextFromJson(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        if (field != null && !field.isNull()) {
            return field.asText();
        }
        return defaultValue;
    }

    private List<String> parseJsonArray(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private String fixTruncatedJson(String json) {
        if (json == null || json.isEmpty()) {
            return "{}";
        }
        String trimmed = json.trim();

        // Đếm số dấu { và }
        int openBraces = 0;
        int closeBraces = 0;
        for (char c : trimmed.toCharArray()) {
            if (c == '{')
                openBraces++;
            if (c == '}')
                closeBraces++;
        }

        // Thêm dấu } nếu thiếu
        StringBuilder fixed = new StringBuilder(trimmed);
        while (closeBraces < openBraces) {
            // Kiểm tra xem có cần thêm dấu " để đóng string không
            if (trimmed.endsWith("\"")) {
                // String đã đóng, chỉ cần thêm }
            } else {
                // Có thể string đang mở, thêm " trước
                fixed.append("\"");
            }
            fixed.append("}");
            closeBraces++;
        }

        logger.info("Fixed truncated JSON: added {} closing braces",
                openBraces - (closeBraces - (openBraces - closeBraces)));
        return fixed.toString();
    }

    private Innovation findInnovationById(String innovationId) {
        return innovationRepository.findById(innovationId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy sáng kiến với ID: " + innovationId));
    }

    private String extractInnovationContent(Innovation innovation) {
        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("Tên sáng kiến: ").append(innovation.getInnovationName()).append("\n\n");

        if (innovation.getBasisText() != null && !innovation.getBasisText().isEmpty()) {
            contentBuilder.append("Căn cứ: ").append(innovation.getBasisText()).append("\n\n");
        }

        List<FormData> formDataList = innovation.getFormDataList();
        if (formDataList != null && !formDataList.isEmpty()) {
            contentBuilder.append("Nội dung chi tiết:\n");
            for (FormData formData : formDataList) {
                String fieldLabel = formData.getFormField() != null ? formData.getFormField().getLabel() : "Trường";
                String fieldValue = extractFieldValue(formData.getFieldValue());

                if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                    contentBuilder.append("- ").append(fieldLabel).append(": ").append(fieldValue).append("\n");
                }
            }
        }

        return contentBuilder.toString();
    }

    private String extractFieldValue(JsonNode fieldValue) {
        if (fieldValue == null) {
            return null;
        }

        if (fieldValue.isTextual()) {
            return fieldValue.asText();
        }

        if (fieldValue.isNumber()) {
            return fieldValue.asText();
        }

        if (fieldValue.isBoolean()) {
            return fieldValue.asBoolean() ? "Có" : "Không";
        }

        if (fieldValue.isArray()) {
            StringBuilder arrayContent = new StringBuilder();
            for (JsonNode item : fieldValue) {
                if (item.isTextual()) {
                    if (arrayContent.length() > 0) {
                        arrayContent.append(", ");
                    }
                    arrayContent.append(item.asText());
                } else if (item.isObject()) {
                    if (arrayContent.length() > 0) {
                        arrayContent.append("; ");
                    }
                    arrayContent.append(extractObjectContent(item));
                }
            }
            return arrayContent.toString();
        }

        if (fieldValue.isObject()) {
            return extractObjectContent(fieldValue);
        }

        return fieldValue.toString();
    }

    private String extractObjectContent(JsonNode object) {
        StringBuilder content = new StringBuilder();
        Iterator<String> fieldNames = object.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (content.length() > 0) {
                content.append(", ");
            }
            content.append(fieldName).append(": ").append(object.get(fieldName).asText());
        }
        return content.toString();
    }
}
