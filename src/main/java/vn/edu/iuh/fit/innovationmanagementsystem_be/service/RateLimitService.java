package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.config.RateLimitConfig;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    public boolean isAllowed(String key) {
        if (!rateLimitConfig.isEnabled()) {
            return true;
        }

        String redisKey = RATE_LIMIT_PREFIX + key;
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            return true;
        }

        if (currentCount == 1) {
            redisTemplate.expire(redisKey, rateLimitConfig.getWindowDurationSeconds(), TimeUnit.SECONDS);
        }

        boolean allowed = currentCount <= rateLimitConfig.getRequestsPerWindow();

        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}, count: {}", key, currentCount);
        }

        return allowed;
    }

    public int getRemainingRequests(String key) {
        if (!rateLimitConfig.isEnabled()) {
            return rateLimitConfig.getRequestsPerWindow();
        }

        String redisKey = RATE_LIMIT_PREFIX + key;
        Object value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            return rateLimitConfig.getRequestsPerWindow();
        }

        int currentCount = Integer.parseInt(value.toString());
        return Math.max(0, rateLimitConfig.getRequestsPerWindow() - currentCount);
    }

    public long getRetryAfterSeconds(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : rateLimitConfig.getWindowDurationSeconds();
    }
}
