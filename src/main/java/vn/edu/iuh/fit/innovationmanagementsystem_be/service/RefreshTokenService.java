package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKENS_PREFIX = "user_refresh_tokens:";

    /**
     * Tạo refresh token mới và lưu vào Redis
     */
    public String createRefreshToken(String userId) {
        String refreshToken = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + refreshToken;

        // Lưu refresh token với userId
        redisTemplate.opsForValue().set(key, userId, refreshExpiration, TimeUnit.MILLISECONDS);

        // Lưu refresh token vào set của user (để có thể logout tất cả thiết bị)
        String userTokensKey = USER_REFRESH_TOKENS_PREFIX + userId;
        redisTemplate.opsForSet().add(userTokensKey, refreshToken);
        redisTemplate.expire(userTokensKey, refreshExpiration, TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    /**
     * Xác thực refresh token và trả về userId
     */
    public String validateRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        Object userId = redisTemplate.opsForValue().get(key);
        return userId != null ? userId.toString() : null;
    }

    /**
     * Xóa refresh token cụ thể (logout)
     */
    public void deleteRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;

        // Lấy userId trước khi xóa
        Object userId = redisTemplate.opsForValue().get(key);

        // Xóa refresh token
        redisTemplate.delete(key);

        // Xóa khỏi set của user
        if (userId != null) {
            String userTokensKey = USER_REFRESH_TOKENS_PREFIX + userId.toString();
            redisTemplate.opsForSet().remove(userTokensKey, refreshToken);
        }
    }

    /**
     * Xóa tất cả refresh tokens của user (logout all devices)
     */
    public void deleteAllRefreshTokensForUser(String userId) {
        String userTokensKey = USER_REFRESH_TOKENS_PREFIX + userId;

        // Lấy tất cả refresh tokens của user
        var refreshTokens = redisTemplate.opsForSet().members(userTokensKey);

        if (refreshTokens != null) {
            // Xóa từng refresh token
            for (Object token : refreshTokens) {
                String key = REFRESH_TOKEN_PREFIX + token.toString();
                redisTemplate.delete(key);
            }
        }

        // Xóa set của user
        redisTemplate.delete(userTokensKey);
    }

    /**
     * Rotate refresh token (tạo mới và xóa cũ)
     */
    public String rotateRefreshToken(String oldRefreshToken) {
        String userId = validateRefreshToken(oldRefreshToken);
        if (userId != null) {
            // Xóa token cũ
            deleteRefreshToken(oldRefreshToken);
            // Tạo token mới
            return createRefreshToken(userId);
        }
        return null;
    }

    /**
     * Kiểm tra refresh token có tồn tại không
     */
    public boolean existsRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Lấy số lượng refresh tokens của user
     */
    public long getRefreshTokenCountForUser(String userId) {
        String userTokensKey = USER_REFRESH_TOKENS_PREFIX + userId;
        Long count = redisTemplate.opsForSet().size(userTokensKey);
        return count != null ? count : 0;
    }
}
