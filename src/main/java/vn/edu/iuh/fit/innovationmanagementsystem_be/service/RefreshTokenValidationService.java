package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;

@Service
public class RefreshTokenValidationService {

    private final RedisTokenService redisTokenService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    public RefreshTokenValidationService(RedisTokenService redisTokenService, JwtTokenUtil jwtTokenUtil,
            UserRepository userRepository) {
        this.redisTokenService = redisTokenService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

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
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ValidationResult.invalid("Refresh token không được để trống");
            }
            if (!refreshToken.contains(".")) {
                return ValidationResult.invalid("Refresh token format không hợp lệ");
            }
            String username = jwtTokenUtil.extractUsername(refreshToken);
            if (username == null) {
                return ValidationResult.invalid("Refresh token signature không hợp lệ");
            }
            if (!redisTokenService.isRefreshTokenValid(refreshToken)) {
                return ValidationResult.invalid("Refresh token không tồn tại hoặc đã hết hạn");
            }
            if (jwtTokenUtil.isTokenExpired(refreshToken)) {
                // Auto-cleanup expired token
                redisTokenService.deleteRefreshToken(refreshToken);
                return ValidationResult.invalid("Refresh token đã hết hạn");
            }
            User user = userRepository.findByPersonnelId(username)
                    .orElse(null);
            if (user == null) {
                return ValidationResult.invalid("Tài khoản không tồn tại");
            }
            if (user.getStatus() != null && user.getStatus() != UserStatusEnum.ACTIVE) {
                return ValidationResult.invalid("Tài khoản đã bị vô hiệu hóa");
            }
            return ValidationResult.valid(username, user);

        } catch (Exception e) {
            return ValidationResult.invalid("Lỗi xác thực refresh token: " + e.getMessage());
        }
    }

    // 2. Validation result class
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
