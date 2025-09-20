package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitingService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String OTP_RATE_LIMIT_PREFIX = "rate_limit:otp:";
    private static final int MAX_REQUESTS_PER_HOUR = 3;
    private static final long RATE_LIMIT_WINDOW = 3600; // 1 giờ = 3600 giây

    // 1. Kiểm tra rate limiting cho OTP
    public boolean isOtpRateLimited(String personnelId) {
        String key = OTP_RATE_LIMIT_PREFIX + personnelId;

        try {
            // Lấy số lần request hiện tại
            Object currentCount = redisTemplate.opsForValue().get(key);
            int count = currentCount != null ? Integer.parseInt(currentCount.toString()) : 0;

            if (count >= MAX_REQUESTS_PER_HOUR) {
                return true; // Bị rate limit
            }

            // Tăng counter
            if (count == 0) {
                // Lần đầu tiên, set counter = 1 và TTL = 1 giờ
                redisTemplate.opsForValue().set(key, 1, RATE_LIMIT_WINDOW, TimeUnit.SECONDS);
            } else {
                // Tăng counter, giữ nguyên TTL
                redisTemplate.opsForValue().increment(key);
            }

            return false; // Không bị rate limit

        } catch (Exception e) {
            // Nếu có lỗi, cho phép request (fail-safe)
            return false;
        }
    }

    // 2. Reset rate limit counter cho personnelId (dùng để test hoặc admin reset)
    public void resetRateLimit(String personnelId) {
        String key = OTP_RATE_LIMIT_PREFIX + personnelId;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
        }
    }

    // 3. Lấy thông tin rate limit hiện tại
    public RateLimitInfo getRateLimitInfo(String personnelId) {
        String key = OTP_RATE_LIMIT_PREFIX + personnelId;

        try {
            Object currentCount = redisTemplate.opsForValue().get(key);
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            int count = currentCount != null ? Integer.parseInt(currentCount.toString()) : 0;
            long remainingTime = ttl != null ? ttl : 0;

            return new RateLimitInfo(count, MAX_REQUESTS_PER_HOUR, remainingTime);

        } catch (Exception e) {
            return new RateLimitInfo(0, MAX_REQUESTS_PER_HOUR, 0);
        }
    }

    // 4. DTO cho thông tin rate limit
    public static class RateLimitInfo {
        private final int currentCount;
        private final int maxRequests;
        private final long remainingTimeSeconds;

        public RateLimitInfo(int currentCount, int maxRequests, long remainingTimeSeconds) {
            this.currentCount = currentCount;
            this.maxRequests = maxRequests;
            this.remainingTimeSeconds = remainingTimeSeconds;
        }

        public int getCurrentCount() {
            return currentCount;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public long getRemainingTimeSeconds() {
            return remainingTimeSeconds;
        }

        public boolean isLimited() {
            return currentCount >= maxRequests;
        }
    }
}
