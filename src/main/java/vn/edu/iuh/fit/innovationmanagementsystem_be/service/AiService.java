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
            AiAnalysisResult result = AiAnalysisResult.builder()
                    .innovationId(innovationId)
                    .innovationName(innovationName)
                    .contentHash(contentHash)
                    .summary(response.getSummary())
                    .keyPoints(objectMapper.writeValueAsString(response.getKeyPoints()))
                    .strengths(objectMapper.writeValueAsString(response.getStrengths()))
                    .weaknesses(objectMapper.writeValueAsString(response.getWeaknesses()))
                    .suggestions(objectMapper.writeValueAsString(response.getSuggestions()))
                    .analysis(response.getAnalysis())
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
            return AiAnalysisResponse.builder()
                    .innovationId(result.getInnovationId())
                    .innovationName(result.getInnovationName())
                    .summary(result.getSummary())
                    .keyPoints(parseJsonList(result.getKeyPoints()))
                    .strengths(parseJsonList(result.getStrengths()))
                    .weaknesses(parseJsonList(result.getWeaknesses()))
                    .suggestions(parseJsonList(result.getSuggestions()))
                    .analysis(result.getAnalysis())
                    .generatedAt(result.getGeneratedAt())
                    .build();
        } catch (Exception e) {
            logger.error("Lỗi khi convert AiAnalysisResult sang Response: {}", e.getMessage());
            return AiAnalysisResponse.builder()
                    .innovationId(result.getInnovationId())
                    .innovationName(result.getInnovationName())
                    .summary(result.getSummary())
                    .keyPoints(new ArrayList<>())
                    .strengths(new ArrayList<>())
                    .weaknesses(new ArrayList<>())
                    .suggestions(new ArrayList<>())
                    .analysis(result.getAnalysis())
                    .generatedAt(result.getGeneratedAt())
                    .build();
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            logger.warn("Lỗi khi parse JSON list: {}", e.getMessage());
            return new ArrayList<>();
        }
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
