package vn.edu.iuh.fit.innovationmanagementsystem_be.integration.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.RedisTokenService;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DisplayName("RedisTokenService Integration Tests")
class RedisTokenServiceIntegrationTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private RedisTokenService redisTokenService;
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Setup Redis connection
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisContainer.getHost());
        config.setPort(redisContainer.getMappedPort(6379));

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();

        redisTokenService = new RedisTokenService(redisTemplate);

        // Clear all keys
        var keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // ==================== Save Refresh Token Tests ====================

    @Test
    @DisplayName("1. Save Refresh Token - Should store token in Redis")
    void testSaveRefreshToken_Success() {
        String userId = "user-123";
        String refreshToken = "refresh-token-abc";
        long ttl = 3600;

        redisTokenService.saveRefreshToken(userId, refreshToken, ttl);

        String storedUserId = redisTokenService.getUserIdFromRefreshToken(refreshToken);
        assertEquals(userId, storedUserId);
    }

    // ==================== Get UserId From Refresh Token Tests ====================

    @Test
    @DisplayName("2. Get UserId From Refresh Token - Should return userId")
    void testGetUserIdFromRefreshToken_Success() {
        String userId = "user-456";
        String refreshToken = "refresh-token-xyz";
        redisTokenService.saveRefreshToken(userId, refreshToken, 3600);

        String result = redisTokenService.getUserIdFromRefreshToken(refreshToken);

        assertEquals(userId, result);
    }

    @Test
    @DisplayName("3. Get UserId From Refresh Token - Should return null for non-existent token")
    void testGetUserIdFromRefreshToken_NotFound() {
        String result = redisTokenService.getUserIdFromRefreshToken("non-existent-token");

        assertNull(result);
    }

    // ==================== Is Refresh Token Valid Tests ====================

    @Test
    @DisplayName("4. Is Refresh Token Valid - Should return true for valid token")
    void testIsRefreshTokenValid_True() {
        String userId = "user-789";
        String refreshToken = "valid-refresh-token";
        redisTokenService.saveRefreshToken(userId, refreshToken, 3600);

        boolean isValid = redisTokenService.isRefreshTokenValid(refreshToken);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("5. Is Refresh Token Valid - Should return false for invalid token")
    void testIsRefreshTokenValid_False() {
        boolean isValid = redisTokenService.isRefreshTokenValid("invalid-token");

        assertFalse(isValid);
    }

    // ==================== Delete Refresh Token Tests ====================

    @Test
    @DisplayName("6. Delete Refresh Token - Should remove token from Redis")
    void testDeleteRefreshToken_Success() {
        String userId = "user-delete";
        String refreshToken = "token-to-delete";
        redisTokenService.saveRefreshToken(userId, refreshToken, 3600);

        redisTokenService.deleteRefreshToken(refreshToken);

        assertFalse(redisTokenService.isRefreshTokenValid(refreshToken));
    }

    // ==================== Blacklist Access Token Tests ====================

    @Test
    @DisplayName("7. Blacklist Access Token - Should add token to blacklist")
    void testBlacklistAccessToken_Success() {
        String accessToken = "access-token-to-blacklist";

        redisTokenService.blacklistAccessToken(accessToken, 3600);

        assertTrue(redisTokenService.isAccessTokenBlacklisted(accessToken));
    }

    @Test
    @DisplayName("8. Is Access Token Blacklisted - Should return false for non-blacklisted token")
    void testIsAccessTokenBlacklisted_False() {
        boolean isBlacklisted = redisTokenService.isAccessTokenBlacklisted("normal-token");

        assertFalse(isBlacklisted);
    }

    // ==================== Update Refresh Token Tests ====================

    @Test
    @DisplayName("9. Update Refresh Token - Should replace old token with new")
    void testUpdateRefreshToken_Success() {
        String userId = "user-update";
        String oldToken = "old-refresh-token";
        String newToken = "new-refresh-token";
        redisTokenService.saveRefreshToken(userId, oldToken, 3600);

        redisTokenService.updateRefreshToken(userId, oldToken, newToken, 3600);

        assertFalse(redisTokenService.isRefreshTokenValid(oldToken));
        assertTrue(redisTokenService.isRefreshTokenValid(newToken));
        assertEquals(userId, redisTokenService.getUserIdFromRefreshToken(newToken));
    }

    // ==================== TTL Tests ====================

    @Test
    @DisplayName("10. Get Refresh Token TTL - Should return remaining TTL")
    void testGetRefreshTokenTTL_Success() {
        String userId = "user-ttl";
        String refreshToken = "ttl-test-token";
        long ttl = 3600;
        redisTokenService.saveRefreshToken(userId, refreshToken, ttl);

        Long remainingTtl = redisTokenService.getRefreshTokenTTL(refreshToken);

        assertNotNull(remainingTtl);
        assertTrue(remainingTtl > 0 && remainingTtl <= ttl);
    }
}
