package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AiAnalysisResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.ai.AiProvider;

import java.util.Iterator;
import java.util.List;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final AiProvider aiProvider;
    private final InnovationRepository innovationRepository;
    private final AiAnalysisCacheService cacheService;

    public AiService(AiProvider aiProvider, InnovationRepository innovationRepository,
            AiAnalysisCacheService cacheService) {
        this.aiProvider = aiProvider;
        this.innovationRepository = innovationRepository;
        this.cacheService = cacheService;
        logger.info("AiService initialized with provider: {}", aiProvider.getProviderName());
    }

    public AiAnalysisResponse analyzeInnovation(String innovationId) {
        Innovation innovation = findInnovationById(innovationId);
        String content = extractInnovationContent(innovation);

        if (content == null || content.trim().isEmpty()) {
            throw new IdInvalidException("Sáng kiến không có nội dung để phân tích");
        }

        String contentHash = cacheService.generateContentHash(content);
        AiAnalysisResponse cachedResponse = cacheService.getCachedAnalysis(innovationId, contentHash);
        if (cachedResponse != null) {
            logger.info("Trả về kết quả từ cache cho innovation: {}", innovationId);
            return cachedResponse;
        }

        logger.info("Analyzing innovation: {} using provider: {}", innovation.getInnovationName(),
                aiProvider.getProviderName());
        AiAnalysisResponse response = aiProvider.analyze(innovationId, innovation.getInnovationName(), content);

        cacheService.cacheAnalysis(innovationId, contentHash, response);

        return response;
    }

    public void preComputeAnalysis(String innovationId) {
        try {
            Innovation innovation = findInnovationById(innovationId);
            String content = extractInnovationContent(innovation);

            if (content == null || content.trim().isEmpty()) {
                logger.warn("Innovation {} không có nội dung để pre-compute analysis", innovationId);
                return;
            }

            String contentHash = cacheService.generateContentHash(content);
            if (cacheService.getCachedAnalysis(innovationId, contentHash) != null) {
                logger.info("Đã có cache cho innovation: {}, bỏ qua pre-compute", innovationId);
                return;
            }

            logger.info("Pre-computing analysis cho innovation: {}", innovationId);
            AiAnalysisResponse response = aiProvider.analyze(innovationId, innovation.getInnovationName(), content);
            cacheService.cacheAnalysis(innovationId, contentHash, response);
            logger.info("Pre-compute analysis hoàn thành cho innovation: {}", innovationId);
        } catch (Exception e) {
            logger.error("Lỗi khi pre-compute analysis cho innovation: {} - {}", innovationId, e.getMessage());
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
