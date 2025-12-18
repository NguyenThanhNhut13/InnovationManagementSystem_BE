package vn.edu.iuh.fit.innovationmanagementsystem_be.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AiAnalysisResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OllamaProvider implements AiProvider {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.api.url:http://ollama:11434/api/generate}")
    private String apiUrl;

    @Value("${ollama.model:qwen2.5:3b}")
    private String model;

    @Value("${ollama.options.temperature:0.3}")
    private double temperature;

    @Value("${ollama.options.top_p:0.7}")
    private double topP;

    @Value("${ollama.options.num_ctx:1024}")
    private int numCtx;

    @Value("${ollama.options.num_thread:7}")
    private int numThread;

    @Value("${ollama.options.num_predict:350}")
    private int numPredict;

    public OllamaProvider(ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public boolean isAvailable() {
        try {
            String healthUrl = apiUrl.replace("/api/generate", "/api/tags");
            webClient.get()
                    .uri(healthUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateContent(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            Map<String, Object> options = new HashMap<>();
            options.put("temperature", temperature);
            options.put("top_p", topP);
            options.put("num_ctx", numCtx);
            options.put("num_thread", numThread);
            options.put("num_predict", numPredict);
            requestBody.put("options", options);

            String response = webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMinutes(3))
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode responseNode = jsonResponse.get("response");
            if (responseNode != null) {
                return responseNode.asText();
            }

            return null;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Lỗi khi gọi Ollama API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi Ollama API: " + e.getMessage());
        }
    }

    @Override
    public AiAnalysisResponse analyze(String innovationId, String innovationName, String content) {
        String prompt = buildAnalysisPrompt(content);
        String response = generateContent(prompt);
        return parseAnalysisResponse(innovationId, innovationName, response);
    }

    private String buildAnalysisPrompt(String content) {
        return """
                Bạn là một chuyên gia đánh giá sáng kiến khoa học. Hãy tóm tắt và phân tích sáng kiến sau đây. Bạn phải luôn trả lời bằng tiếng Việt.

                NỘI DUNG SÁNG KIẾN:
                %s

                YÊU CẦU:
                Tóm tắt và đánh giá sáng kiến, trả về kết quả theo định dạng JSON sau (KHÔNG thêm markdown code block):
                {
                    "summary": "Đoạn tóm tắt ngắn gọn khoảng 100 từ về nội dung và mục tiêu của sáng kiến",
                    "keyPoints": ["Điểm chính 1", "Điểm chính 2", "Điểm chính 3", "Điểm chính 4", "Điểm chính 5"],
                    "strengths": ["Điểm mạnh 1", "Điểm mạnh 2", "Điểm mạnh 3"],
                    "weaknesses": ["Điểm yếu 1", "Điểm yếu 2"],
                    "suggestions": ["Gợi ý cải thiện 1", "Gợi ý cải thiện 2", "Gợi ý cải thiện 3"],
                    "analysis": "Phân tích chi tiết về sáng kiến khoảng 100 từ"
                }

                Chỉ trả về JSON, không thêm bất kỳ text nào khác.
                """
                .formatted(content);
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
            return AiAnalysisResponse.builder()
                    .innovationId(innovationId)
                    .innovationName(innovationName)
                    .summary("")
                    .keyPoints(new ArrayList<>())
                    .strengths(new ArrayList<>())
                    .weaknesses(new ArrayList<>())
                    .suggestions(new ArrayList<>())
                    .analysis(response != null ? response : "Không thể phân tích sáng kiến")
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
