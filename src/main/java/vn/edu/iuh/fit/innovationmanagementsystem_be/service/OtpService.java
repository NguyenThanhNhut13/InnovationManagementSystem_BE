package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;

import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;

    public OtpService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL = 5 * 60; // 5 phút

    // 1. Generate OTP 6 numbers randomly
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append((int) (Math.random() * 10));
        }
        return otp.toString();
    }

    // 2. Save OTP to Redis with TTL 5 minutes
    public void saveOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        try {
            redisTemplate.opsForValue().set(key, otp, OTP_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể lưu OTP", e);
        }
    }

    // 3. Validate OTP from user
    public boolean validateOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        try {
            Object savedOtp = redisTemplate.opsForValue().get(key);
            if (savedOtp != null && savedOtp.toString().equals(otp)) {
                // Xóa OTP sau khi validate thành công
                redisTemplate.delete(key);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 4. Lấy TTL còn lại của OTP
    public Long getOtpTTL(String email) {
        String key = OTP_PREFIX + email;
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    // 5. Delete OTP from Redis
    public void deleteOtp(String email) {
        String key = OTP_PREFIX + email;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {

        }
    }

    // 6. Check if OTP exists
    public boolean isOtpExists(String email) {
        String key = OTP_PREFIX + email;
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            return false;
        }
    }
}
