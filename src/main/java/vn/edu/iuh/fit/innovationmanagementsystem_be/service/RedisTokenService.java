package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";

    /**
     * Lưu refresh token vào Redis với TTL
     */
    public void saveRefreshToken(String userId, String refreshToken, long ttlInSeconds) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            redisTemplate.opsForValue().set(key, userId, ttlInSeconds, TimeUnit.SECONDS);
            log.info("Saved refresh token for user: {} with TTL: {} seconds", userId, ttlInSeconds);
        } catch (Exception e) {
            log.error("Error saving refresh token for user: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("Không thể lưu refresh token", e);
        }
    }

    /**
     * Lấy userId từ refresh token
     */
    public String getUserIdFromRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Error getting userId from refresh token, error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra refresh token có tồn tại và hợp lệ không
     */
    public boolean isRefreshTokenValid(String refreshToken) {
        String userId = getUserIdFromRefreshToken(refreshToken);
        return userId != null;
    }

    /**
     * Xóa refresh token khỏi Redis
     */
    public void deleteRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            redisTemplate.delete(key);
            log.info("Deleted refresh token");
        } catch (Exception e) {
            log.error("Error deleting refresh token, error: {}", e.getMessage());
        }
    }

    /**
     * Cập nhật refresh token mới
     */
    public void updateRefreshToken(String userId, String oldRefreshToken, String newRefreshToken, long ttlInSeconds) {
        try {
            // Xóa token cũ
            deleteRefreshToken(oldRefreshToken);

            // Lưu token mới
            saveRefreshToken(userId, newRefreshToken, ttlInSeconds);

            log.info("Updated refresh token for user: {}, TTL: {} seconds", userId, ttlInSeconds);
        } catch (Exception e) {
            log.error("Error updating refresh token for user: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("Không thể cập nhật refresh token", e);
        }
    }

    /**
     * Blacklist access token (khi logout)
     */
    public void blacklistAccessToken(String accessToken, long ttlInSeconds) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        try {
            redisTemplate.opsForValue().set(key, "blacklisted", ttlInSeconds, TimeUnit.SECONDS);
            log.info("Blacklisted access token with TTL: {} seconds", ttlInSeconds);
        } catch (Exception e) {
            log.error("Error blacklisting access token, error: {}", e.getMessage());
        }
    }

    /**
     * Kiểm tra access token có bị blacklist không
     */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking blacklisted access token, error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lấy TTL còn lại của refresh token
     */
    public Long getRefreshTokenTTL(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for refresh token, error: {}", e.getMessage());
            return null;
        }
    }
}
