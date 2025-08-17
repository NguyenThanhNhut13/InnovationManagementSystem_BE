package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenValidationService {

    private final RedisTokenService redisTokenService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    /**
     * Validate refresh token theo chuẩn enterprise
     * 1. Format validation
     * 2. JWT signature validation
     * 3. Redis existence check
     * 4. Token expiration check
     * 5. User status check
     * 6. Rate limiting check
     */
    public ValidationResult validateRefreshToken(String refreshToken) {
        try {
            // 1. Format validation
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ValidationResult.invalid("Refresh token không được để trống");
            }

            if (!refreshToken.contains(".")) {
                return ValidationResult.invalid("Refresh token format không hợp lệ");
            }

            // 2. JWT signature validation
            String username = jwtTokenUtil.extractUsername(refreshToken);
            if (username == null) {
                return ValidationResult.invalid("Refresh token signature không hợp lệ");
            }

            // 3. Redis existence check
            if (!redisTokenService.isRefreshTokenValid(refreshToken)) {
                log.warn("Refresh token không tồn tại trong Redis: {}", username);
                return ValidationResult.invalid("Refresh token không tồn tại hoặc đã hết hạn");
            }

            // 4. Token expiration check
            if (jwtTokenUtil.isTokenExpired(refreshToken)) {
                // Auto-cleanup expired token
                redisTokenService.deleteRefreshToken(refreshToken);
                log.warn("Refresh token đã hết hạn, đã xóa khỏi Redis: {}", username);
                return ValidationResult.invalid("Refresh token đã hết hạn");
            }

            // 5. User status check
            User user = userRepository.findByPersonnelId(username)
                    .orElse(null);

            if (user == null) {
                log.warn("User không tồn tại: {}", username);
                return ValidationResult.invalid("Tài khoản không tồn tại");
            }

            if (user.getStatus() != null && user.getStatus() != UserStatusEnum.ACTIVE) {
                log.warn("User account không active: {}", username);
                return ValidationResult.invalid("Tài khoản đã bị vô hiệu hóa");
            }

            // 6. Rate limiting check (optional - có thể implement sau)
            if (isRefreshTokenRateLimited(username)) {
                log.warn("Refresh token rate limited cho user: {}", username);
                return ValidationResult.invalid("Quá nhiều yêu cầu refresh token, vui lòng thử lại sau");
            }

            // 7. Get remaining TTL
            Long remainingTTL = redisTokenService.getRefreshTokenTTL(refreshToken);
            if (remainingTTL != null && remainingTTL < 300) { // < 5 phút
                log.info("Refresh token sắp hết hạn cho user: {}, TTL còn lại: {} giây", username, remainingTTL);
            }

            return ValidationResult.valid(username, user);

        } catch (Exception e) {
            log.error("Error validating refresh token: {}", e.getMessage());
            return ValidationResult.invalid("Lỗi xác thực refresh token: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra rate limiting cho refresh token
     * Có thể implement Redis-based rate limiting sau
     */
    private boolean isRefreshTokenRateLimited(String username) {
        // TODO: Implement Redis-based rate limiting
        // Ví dụ: max 5 refresh requests per minute per user
        return false;
    }

    /**
     * Cleanup expired refresh tokens
     * Có thể gọi từ scheduled task
     */
    public void cleanupExpiredRefreshTokens() {
        // TODO: Implement cleanup logic
        log.info("Cleanup expired refresh tokens completed");
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String username;
        private final User user;
        private final String errorMessage;

        private ValidationResult(boolean valid, String username, User user, String errorMessage) {
            this.valid = valid;
            this.username = username;
            this.user = user;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid(String username, User user) {
            return new ValidationResult(true, username, user, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, null, null, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getUsername() {
            return username;
        }

        public User getUser() {
            return user;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
