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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.EmbeddingBatchRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.EmbeddingRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.EmbeddingBatchResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.EmbeddingResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://ai-embedding-service:8000}")
    private String aiServiceUrl;

    public EmbeddingService(ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Kiểm tra xem AI Embedding Service có sẵn sàng không
     */
    public boolean isAvailable() {
        try {
            String healthUrl = aiServiceUrl + "/health";
            String response = webClient.get()
                    .uri(healthUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("status") != null && 
                   "ok".equals(jsonNode.get("status").asText());
        } catch (Exception e) {
            logger.warn("AI Embedding Service is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate embedding cho một text
     */
    public EmbeddingResponse generateEmbedding(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Text cannot be null or empty");
            }

            logger.info("Generating embedding for text (length: {})", text.length());
            long startTime = System.currentTimeMillis();

            EmbeddingRequest request = new EmbeddingRequest(text);
            
            String response = webClient.post()
                    .uri(aiServiceUrl + "/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Embedding generated in {} ms", duration);

            JsonNode jsonNode = objectMapper.readTree(response);
            List<Double> embedding = parseEmbeddingList(jsonNode.get("embedding"));
            Integer dimension = jsonNode.get("dimension") != null ? 
                    jsonNode.get("dimension").asInt() : embedding.size();

            return EmbeddingResponse.builder()
                    .embedding(embedding)
                    .dimension(dimension)
                    .build();

        } catch (WebClientResponseException e) {
            logger.error("AI Embedding Service error: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi khi gọi AI Embedding Service: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error calling AI Embedding Service", e);
            throw new RuntimeException("Lỗi khi gọi AI Embedding Service: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embeddings cho nhiều texts (batch)
     */
    public EmbeddingBatchResponse generateEmbeddingsBatch(List<String> texts) {
        try {
            if (texts == null || texts.isEmpty()) {
                throw new IllegalArgumentException("Texts cannot be null or empty");
            }

            logger.info("Generating embeddings for {} texts", texts.size());
            long startTime = System.currentTimeMillis();

            EmbeddingBatchRequest request = new EmbeddingBatchRequest(texts);
            
            String response = webClient.post()
                    .uri(aiServiceUrl + "/embed/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMinutes(2))
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Batch embeddings generated in {} ms", duration);

            JsonNode jsonNode = objectMapper.readTree(response);
            List<List<Double>> embeddings = parseEmbeddingsList(jsonNode.get("embeddings"));
            Integer count = jsonNode.get("count") != null ? 
                    jsonNode.get("count").asInt() : embeddings.size();
            Integer dimension = jsonNode.get("dimension") != null ? 
                    jsonNode.get("dimension").asInt() : 
                    (embeddings.isEmpty() ? 0 : embeddings.get(0).size());

            return EmbeddingBatchResponse.builder()
                    .embeddings(embeddings)
                    .count(count)
                    .dimension(dimension)
                    .build();

        } catch (WebClientResponseException e) {
            logger.error("AI Embedding Service error: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi khi gọi AI Embedding Service: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error calling AI Embedding Service", e);
            throw new RuntimeException("Lỗi khi gọi AI Embedding Service: " + e.getMessage(), e);
        }
    }

    /**
     * Parse embedding list từ JSON
     */
    private List<Double> parseEmbeddingList(JsonNode node) {
        List<Double> embedding = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                embedding.add(item.asDouble());
            }
        }
        return embedding;
    }

    /**
     * Parse embeddings list (batch) từ JSON
     */
    private List<List<Double>> parseEmbeddingsList(JsonNode node) {
        List<List<Double>> embeddings = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode embeddingNode : node) {
                List<Double> embedding = new ArrayList<>();
                if (embeddingNode.isArray()) {
                    for (JsonNode item : embeddingNode) {
                        embedding.add(item.asDouble());
                    }
                }
                embeddings.add(embedding);
            }
        }
        return embeddings;
    }
}

