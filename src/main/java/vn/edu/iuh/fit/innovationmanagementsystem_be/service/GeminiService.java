package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AiAnalysisResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiService(ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = objectMapper;
    }

    public String generateContent(String prompt) {
        try {
            String fullUrl = apiUrl + "?key=" + apiKey;

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            String response = webClient.post()
                    .uri(fullUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode candidates = jsonResponse.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode contentNode = firstCandidate.get("content");
                if (contentNode != null) {
                    JsonNode partsNode = contentNode.get("parts");
                    if (partsNode != null && partsNode.isArray() && partsNode.size() > 0) {
                        return partsNode.get(0).get("text").asText();
                    }
                }
            }
            return null;
        } catch (WebClientResponseException e) {
            logger.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi khi gọi Gemini API: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error calling Gemini API", e);
            throw new RuntimeException("Lỗi khi gọi Gemini API: " + e.getMessage());
        }
    }

    public AiAnalysisResponse analyze(String innovationId, String innovationName, String content) {
        String prompt = buildAnalysisPrompt(content);
        String response = generateContent(prompt);
        return parseAnalysisResponse(innovationId, innovationName, response);
    }

    private String buildAnalysisPrompt(String content) {
        return """
                Bạn là một chuyên gia đánh giá sáng kiến khoa học. Hãy tóm tắt và phân tích sáng kiến sau đây.

                NỘI DUNG SÁNG KIẾN:
                %s

                YÊU CẦU:
                Tóm tắt và đánh giá sáng kiến, trả về kết quả theo định dạng JSON sau (KHÔNG thêm markdown code block):
                {
                    "summary": "Đoạn tóm tắt ngắn gọn khoảng 100-150 từ về nội dung và mục tiêu của sáng kiến",
                    "keyPoints": ["Điểm chính 1", "Điểm chính 2", "Điểm chính 3", "Điểm chính 4", "Điểm chính 5"],
                    "strengths": ["Điểm mạnh 1", "Điểm mạnh 2", "Điểm mạnh 3"],
                    "weaknesses": ["Điểm yếu 1", "Điểm yếu 2"],
                    "suggestions": ["Gợi ý cải thiện 1", "Gợi ý cải thiện 2", "Gợi ý cải thiện 3"],
                    "analysis": "Phân tích chi tiết về sáng kiến khoảng 150-200 từ"
                }

                Chỉ trả về JSON, không thêm bất kỳ text nào khác.
                """.formatted(content);
    }

    private AiAnalysisResponse parseAnalysisResponse(String innovationId, String innovationName, String response) {
        try {
            String cleanJson = cleanJsonResponse(response);
            JsonNode jsonNode = objectMapper.readTree(cleanJson);

            List<String> keyPoints = parseStringList(jsonNode.get("keyPoints"));
            List<String> strengths = parseStringList(jsonNode.get("strengths"));
            List<String> weaknesses = parseStringList(jsonNode.get("weaknesses"));
            List<String> suggestions = parseStringList(jsonNode.get("suggestions"));

            return AiAnalysisResponse.builder()
                    .innovationId(innovationId)
                    .innovationName(innovationName)
                    .summary(jsonNode.get("summary").asText())
                    .keyPoints(keyPoints)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .suggestions(suggestions)
                    .analysis(jsonNode.get("analysis").asText())
                    .generatedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            logger.error("Error parsing analysis response: {}", response, e);
            return AiAnalysisResponse.builder()
                    .innovationId(innovationId)
                    .innovationName(innovationName)
                    .summary("")
                    .keyPoints(new ArrayList<>())
                    .strengths(new ArrayList<>())
                    .weaknesses(new ArrayList<>())
                    .suggestions(new ArrayList<>())
                    .analysis(response)
                    .generatedAt(LocalDateTime.now())
                    .build();
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null)
            return "{}";
        String cleaned = response.trim();
        Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
        Matcher matcher = pattern.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group(1);
        }
        cleaned = cleaned.replaceAll("```", "").trim();
        return cleaned;
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                list.add(item.asText());
            }
        }
        return list;
    }
}
