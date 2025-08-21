package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.LoginRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.ChangePasswordRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.OtpRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.ResetPasswordWithOtpRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.LoginResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.TokenResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.ChangePasswordResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.OtpResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;
    private final RefreshTokenValidationService refreshTokenValidationService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RateLimitingService rateLimitingService;
    private final OtpService otpService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
            UserRepository userRepository, RedisTokenService redisTokenService,
            RefreshTokenValidationService refreshTokenValidationService, PasswordEncoder passwordEncoder,
            EmailService emailService, RateLimitingService rateLimitingService, OtpService otpService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.redisTokenService = redisTokenService;
        this.refreshTokenValidationService = refreshTokenValidationService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.rateLimitingService = rateLimitingService;
        this.otpService = otpService;
    }

    // 1. Login
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getPersonnelId(),
                            loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByPersonnelId(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy tài khoản với mã nhân viên: " + userDetails.getUsername()));

            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // Lưu refresh token vào Redis với TTL
            long refreshTokenTTL = jwtTokenUtil.getRefreshTokenExpiration() / 1000; // Convert từ milliseconds sang
                                                                                    // seconds
            redisTokenService.saveRefreshToken(user.getPersonnelId(), refreshToken, refreshTokenTTL);

            return buildLoginResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {

            throw new RuntimeException("Thông tin đăng nhập không hợp lệ");
        } catch (Exception e) {

            throw new RuntimeException("Xác thực thất bại: " + e.getMessage());
        }
    }

    // 2. Refresh Token
    public TokenResponse refreshToken(String refreshToken) {
        // Sử dụng RefreshTokenValidationService để validate theo chuẩn enterprise
        RefreshTokenValidationService.ValidationResult validationResult = refreshTokenValidationService
                .validateRefreshToken(refreshToken);

        if (!validationResult.isValid()) {
            throw new RuntimeException(validationResult.getErrorMessage());
        }

        String username = validationResult.getUsername();
        User user = validationResult.getUser();

        // Tạo UserDetails từ User object đã có (không cần query lại database)
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getPersonnelId())
                .password(user.getPassword())
                .authorities(user.getUserRoles() != null ? user.getUserRoles().stream()
                        .map(userRole -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                userRole.getRole().getRoleName().name()))
                        .collect(Collectors.toList())
                        : Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        UserRoleEnum.GIANG_VIEN.name())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        if (jwtTokenUtil.validateToken(refreshToken, userDetails)) {
            String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // Cập nhật refresh token mới trong Redis
            long refreshTokenTTL = jwtTokenUtil.getRefreshTokenExpiration() / 1000;
            redisTokenService.updateRefreshToken(username, refreshToken, newRefreshToken, refreshTokenTTL);

            return new TokenResponse(
                    newAccessToken,
                    newRefreshToken);
        } else {
            throw new RuntimeException("Refresh token không hợp lệ");
        }
    }

    // 3. Đăng xuất - xóa refresh token khỏi Redis và blacklist access token
    public void logout(String accessToken, String refreshToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("Access token không được để trống");
        }
        if (jwtTokenUtil.isTokenExpired(accessToken)) {
            throw new RuntimeException("Access token đã hết hạn");
        }
        String personnelId = extractPersonnelIdFromToken(accessToken);
        if (personnelId == null) {
            throw new RuntimeException("Không thể xác thực access token");
        }
        String refreshTokenOwner = redisTokenService.getUserIdFromRefreshToken(refreshToken);
        if (refreshTokenOwner == null) {
            throw new RuntimeException("Refresh token không tồn tại");
        }

        if (!personnelId.equals(refreshTokenOwner)) {
            throw new RuntimeException("Refresh token không hợp lệ");
        }

        // Delete refresh token from Redis
        redisTokenService.deleteRefreshToken(refreshToken);

        // Blacklist access token với TTL dài hơn để đảm bảo token bị vô hiệu hóa
        // dụng TTL của access token + thêm 1 giờ để đảm bảo an toàn
        long accessTokenTTL = (jwtTokenUtil.getAccessTokenExpiration() / 1000) + 3600; // +1 giờ
        redisTokenService.blacklistAccessToken(accessToken, accessTokenTTL);

    }

    // 4. Extract personnelId từ access token
    public String extractPersonnelIdFromToken(String accessToken) {
        return jwtTokenUtil.extractUsername(accessToken);
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getUserRoles() != null ? user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName().name())
                .collect(Collectors.toList()) : Collections.singletonList(UserRoleEnum.GIANG_VIEN.name());

        return new LoginResponse(
                user.getId(),
                user.getPersonnelId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null,
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                accessToken,
                refreshToken);
    }

    // 5. Đổi mật khẩu
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            throw new RuntimeException("Không thể xác thực người dùng");
        }
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new RuntimeException("Mật khẩu mới không được giống mật khẩu cũ");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }
        User currentUser = userRepository.findByPersonnelId(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        currentUser.setPassword(encodedNewPassword);

        userRepository.save(currentUser);

        return new ChangePasswordResponse(
                "Đổi mật khẩu thành công",
                currentUser.getId(),
                currentUser.getPersonnelId(),
                currentUser.getEmail(),
                java.time.LocalDateTime.now().toString());
    }

    /**
     * Lấy username hiện tại từ SecurityContext
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // 6. Quên mật khẩu - Gửi OTP qua email
    public OtpResponse forgotPassword(OtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email: " + request.getEmail()));

        // Kiểm tra rate limiting (max 3 requests/hour per email)
        if (rateLimitingService.isOtpRateLimited(request.getEmail())) {
            RateLimitingService.RateLimitInfo rateLimitInfo = rateLimitingService.getRateLimitInfo(request.getEmail());

            throw new RuntimeException("Quá nhiều yêu cầu reset password. Vui lòng thử lại sau " +
                    (rateLimitInfo.getRemainingTimeSeconds() / 60) + " phút");
        }

        // Generate OTP 6 số
        String otp = otpService.generateOtp();

        // Lưu OTP vào Redis với TTL 5 phút
        otpService.saveOtp(request.getEmail(), otp);

        // Gửi email OTP
        emailService.sendOtpEmail(user.getEmail(), otp, 5L);

        return new OtpResponse(
                "OTP đã được gửi đến email của bạn",
                user.getEmail(),
                300L); // 5 phút = 300 giây
    }

    // 7. Reset mật khẩu - Sử dụng OTP
    public ChangePasswordResponse resetPassword(ResetPasswordWithOtpRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("OTP không hợp lệ hoặc đã hết hạn");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);

        userRepository.save(user);

        // Gửi email thông báo password đã được đổi
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getPersonnelId());

        return new ChangePasswordResponse(
                "Đặt lại mật khẩu thành công",
                user.getId(),
                user.getPersonnelId(),
                user.getEmail(),
                java.time.LocalDateTime.now().toString());
    }
}
