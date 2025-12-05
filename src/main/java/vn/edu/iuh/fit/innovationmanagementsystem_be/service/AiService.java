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

import java.util.Iterator;
import java.util.List;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final GeminiService geminiService;
    private final InnovationRepository innovationRepository;

    public AiService(GeminiService geminiService, InnovationRepository innovationRepository) {
        this.geminiService = geminiService;
        this.innovationRepository = innovationRepository;
    }

    public AiAnalysisResponse analyzeInnovation(String innovationId) {
        Innovation innovation = findInnovationById(innovationId);
        String content = extractInnovationContent(innovation);

        if (content == null || content.trim().isEmpty()) {
            throw new IdInvalidException("Sáng kiến không có nội dung để phân tích");
        }

        logger.info("Analyzing innovation: {}", innovation.getInnovationName());
        return geminiService.analyze(innovationId, innovation.getInnovationName(), content);
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
