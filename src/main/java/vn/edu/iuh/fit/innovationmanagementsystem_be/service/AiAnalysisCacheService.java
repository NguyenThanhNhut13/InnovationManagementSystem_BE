package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.AiAnalysisResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class AiAnalysisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisCacheService.class);
    private static final String CACHE_KEY_PREFIX = "ai:analysis:";
    private static final String HASH_KEY_PREFIX = "ai:analysis:hash:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.analysis.cache.ttl:86400}")
    private long cacheTtlSeconds;

    public AiAnalysisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public AiAnalysisResponse getCachedAnalysis(String innovationId, String contentHash) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + innovationId;
            String hashKey = HASH_KEY_PREFIX + innovationId;

            String storedHash = (String) redisTemplate.opsForValue().get(hashKey);
            if (storedHash == null || !storedHash.equals(contentHash)) {
                logger.debug("Cache miss hoặc content đã thay đổi cho innovation: {}", innovationId);
                return null;
            }

            String cachedJson = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson == null) {
                return null;
            }

            logger.info("Cache hit cho innovation: {}", innovationId);
            return objectMapper.readValue(cachedJson, AiAnalysisResponse.class);
        } catch (Exception e) {
            logger.error("Lỗi khi đọc cache cho innovation: {}", innovationId, e);
            return null;
        }
    }

    public void cacheAnalysis(String innovationId, String contentHash, AiAnalysisResponse response) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + innovationId;
            String hashKey = HASH_KEY_PREFIX + innovationId;

            String jsonResponse = objectMapper.writeValueAsString(response);

            redisTemplate.opsForValue().set(cacheKey, jsonResponse, cacheTtlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(hashKey, contentHash, cacheTtlSeconds, TimeUnit.SECONDS);

            logger.info("Đã cache kết quả analysis cho innovation: {} (TTL: {}s)", innovationId, cacheTtlSeconds);
        } catch (JsonProcessingException e) {
            logger.error("Lỗi khi cache analysis cho innovation: {}", innovationId, e);
        }
    }

    public void invalidateCache(String innovationId) {
        String cacheKey = CACHE_KEY_PREFIX + innovationId;
        String hashKey = HASH_KEY_PREFIX + innovationId;

        redisTemplate.delete(cacheKey);
        redisTemplate.delete(hashKey);

        logger.info("Đã xóa cache cho innovation: {}", innovationId);
    }

    public String generateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Lỗi khi tạo hash content", e);
            return String.valueOf(content.hashCode());
        }
    }

    public boolean hasCachedAnalysis(String innovationId) {
        String cacheKey = CACHE_KEY_PREFIX + innovationId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }
}
