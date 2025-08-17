package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";

    // 1. Lưu refresh token vào Redis với TTL
    public void saveRefreshToken(String userId, String refreshToken, long ttlInSeconds) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            redisTemplate.opsForValue().set(key, userId, ttlInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Không thể lưu refresh token", e);
        }
    }

    // 2. Lấy userId từ refresh token
    public String getUserIdFromRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // 3. Kiểm tra refresh token có tồn tại và hợp lệ không
    public boolean isRefreshTokenValid(String refreshToken) {
        String userId = getUserIdFromRefreshToken(refreshToken);
        return userId != null;
    }

    // 4. Xóa refresh token khỏi Redis
    public void deleteRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
        }
    }

    // 5. Cập nhật refresh token mới
    public void updateRefreshToken(String userId, String oldRefreshToken, String newRefreshToken, long ttlInSeconds) {
        try {
            // Xóa token cũ
            deleteRefreshToken(oldRefreshToken);

            // Lưu token mới
            saveRefreshToken(userId, newRefreshToken, ttlInSeconds);

        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật refresh token", e);
        }
    }

    // 6. Blacklist access token (khi logout)
    public void blacklistAccessToken(String accessToken, long ttlInSeconds) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        try {
            redisTemplate.opsForValue().set(key, "blacklisted", ttlInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
        }
    }

    // 7. Kiểm tra access token có bị blacklist không
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    // 8. Lấy TTL còn lại của refresh token
    public Long getRefreshTokenTTL(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

}
