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

    // 1. Tạo OTP 6 số ngẫu nhiên
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append((int) (Math.random() * 10));
        }
        return otp.toString();
    }

    // 2. Lưu OTP vào Redis với TTL 5 phút
    public void saveOtp(String personnelId, String otp) {
        String key = OTP_PREFIX + personnelId;
        try {
            redisTemplate.opsForValue().set(key, otp, OTP_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể lưu OTP", e);
        }
    }

    // 3. Kiểm tra OTP từ user
    public boolean validateOtp(String personnelId, String otp) {
        String key = OTP_PREFIX + personnelId;
        try {
            Object savedOtp = redisTemplate.opsForValue().get(key);
            if (savedOtp != null && savedOtp.toString().equals(otp)) {
                redisTemplate.delete(key);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 4. Lấy TTL còn lại của OTP
    public Long getOtpTTL(String personnelId) {
        String key = OTP_PREFIX + personnelId;
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể lấy TTL của OTP: " + e.getMessage());
        }
    }

    // 5. Xóa OTP khỏi Redis
    public void deleteOtp(String personnelId) {
        String key = OTP_PREFIX + personnelId;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new IdInvalidException("Không thể xóa OTP: " + e.getMessage());
        }
    }

    // 6. Kiểm tra nếu OTP tồn tại
    public boolean isOtpExists(String personnelId) {
        String key = OTP_PREFIX + personnelId;
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            return false;
        }
    }
}
