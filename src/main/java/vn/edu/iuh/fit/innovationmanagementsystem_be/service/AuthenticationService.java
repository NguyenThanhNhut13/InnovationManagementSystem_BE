package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.AuthenticationMapper;
import java.util.Collections;
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
    private final AuthenticationMapper authenticationMapper;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
            UserRepository userRepository, RedisTokenService redisTokenService,
            RefreshTokenValidationService refreshTokenValidationService, PasswordEncoder passwordEncoder,
            EmailService emailService, RateLimitingService rateLimitingService, OtpService otpService,
            AuthenticationMapper authenticationMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.redisTokenService = redisTokenService;
        this.refreshTokenValidationService = refreshTokenValidationService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.rateLimitingService = rateLimitingService;
        this.otpService = otpService;
        this.authenticationMapper = authenticationMapper;
    }

    // 1. Đăng nhập
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getPersonnelId(),
                            loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByPersonnelId(userDetails.getUsername())
                    .orElseThrow(() -> new IdInvalidException(
                            "Không tìm thấy tài khoản với mã nhân viên: " + userDetails.getUsername()));

            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // Lưu refresh token vào Redis với TTL
            long refreshTokenTTL = jwtTokenUtil.getRefreshTokenExpiration() / 1000; // Convert milliseconds to seconds
            redisTokenService.saveRefreshToken(user.getPersonnelId(), refreshToken, refreshTokenTTL);

            LoginResponse loginResponse = authenticationMapper.toLoginResponse(user);
            loginResponse.setAccessToken(accessToken);
            loginResponse.setRefreshToken(refreshToken);
            return loginResponse;

        } catch (BadCredentialsException e) {
            throw new IdInvalidException("Thông tin đăng nhập không hợp lệ");
        } catch (Exception e) {
            throw new IdInvalidException("Xác thực thất bại: " + e.getMessage());
        }
    }

    // 2. Refresh Token
    public TokenResponse refreshToken(String refreshToken, String currentAccessToken) {
        if (currentAccessToken == null || currentAccessToken.isEmpty()) {
            throw new IdInvalidException("Access token hiện tại không được để trống");
        }

        if (!jwtTokenUtil.isTokenExpired(currentAccessToken)
                && redisTokenService.isAccessTokenBlacklisted(currentAccessToken)) {
            throw new IdInvalidException("Access token hiện tại đã bị vô hiệu hóa");
        }

        // RefreshTokenValidationService validate theo chuẩn enterprise
        RefreshTokenValidationService.ValidationResult validationResult = refreshTokenValidationService
                .validateRefreshToken(refreshToken);

        if (!validationResult.isValid()) {
            throw new IdInvalidException(validationResult.getErrorMessage());
        }

        String username = validationResult.getUsername();
        User user = validationResult.getUser();

        // Tạo UserDetails từ User object đã có
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

            // Blacklist access token cũ trước khi tạo token mới
            if (!jwtTokenUtil.isTokenExpired(currentAccessToken)) {
                long accessTokenTTL = (jwtTokenUtil.getAccessTokenExpiration() / 1000) + 3600; // +1 giờ
                redisTokenService.blacklistAccessToken(currentAccessToken, accessTokenTTL);
            }

            long refreshTokenTTL = jwtTokenUtil.getRefreshTokenExpiration() / 1000;
            redisTokenService.updateRefreshToken(username, refreshToken, newRefreshToken, refreshTokenTTL);

            return new TokenResponse(
                    newAccessToken,
                    newRefreshToken);
        } else {
            throw new IdInvalidException("Refresh token không hợp lệ");
        }
    }

    // 3. Đăng xuất - xóa refresh token từ Redis - blacklist access token
    public void logout(String accessToken, String refreshToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IdInvalidException("Access token không được để trống");
        }
        if (jwtTokenUtil.isTokenExpired(accessToken)) {
            throw new IdInvalidException("Access token đã hết hạn");
        }
        String personnelId = extractPersonnelIdFromToken(accessToken);
        if (personnelId == null) {
            throw new IdInvalidException("Không thể xác thực access token");
        }
        String refreshTokenOwner = redisTokenService.getUserIdFromRefreshToken(refreshToken);
        if (refreshTokenOwner == null) {
            throw new IdInvalidException("Refresh token không tồn tại");
        }

        if (!personnelId.equals(refreshTokenOwner)) {
            throw new IdInvalidException("Refresh token không hợp lệ");
        }

        redisTokenService.deleteRefreshToken(refreshToken);

        long accessTokenTTL = (jwtTokenUtil.getAccessTokenExpiration() / 1000) + 3600; // +1 giờ
        redisTokenService.blacklistAccessToken(accessToken, accessTokenTTL);

    }

    // 4. Lấy personnelId từ access token
    public String extractPersonnelIdFromToken(String accessToken) {
        return jwtTokenUtil.extractUsername(accessToken);
    }

    // 5. Đổi mật khẩu
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            throw new IdInvalidException("Không thể xác thực người dùng");
        }
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new IdInvalidException("Mật khẩu mới không được giống mật khẩu cũ");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IdInvalidException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }
        User currentUser = userRepository.findByPersonnelId(currentUsername)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy tài khoản"));
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new IdInvalidException("Mật khẩu cũ không đúng");
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

    // 6. Quên mật khẩu - Gửi OTP qua email
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByPersonnelId(request.getPersonnelId())
                .orElseThrow(() -> new IdInvalidException(
                        "Không tìm thấy tài khoản với mã nhân sự: " + request.getPersonnelId()));

        if (rateLimitingService.isOtpRateLimited(user.getEmail())) {
            RateLimitingService.RateLimitInfo rateLimitInfo = rateLimitingService.getRateLimitInfo(user.getEmail());

            throw new IdInvalidException("Quá nhiều yêu cầu reset password. Vui lòng thử lại sau " +
                    (rateLimitInfo.getRemainingTimeSeconds() / 60) + " phút");
        }

        String otp = otpService.generateOtp();
        otpService.saveOtp(user.getEmail(), otp);
        emailService.sendOtpEmail(user.getEmail(), otp, 5L, user.getFullName());

        return new ForgotPasswordResponse(
                "OTP đã được gửi đến email của bạn",
                user.getEmail(),
                300L); // 5 phút = 300 giây
    }

    // 7. Đặt lại mật khẩu - Sử dụng OTP
    public ChangePasswordResponse resetPassword(ResetPasswordWithOtpRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IdInvalidException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        User user = userRepository.findByPersonnelId(request.getPersonnelId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy tài khoản"));

        if (!otpService.validateOtp(user.getEmail(), request.getOtp())) {
            throw new IdInvalidException("OTP không hợp lệ hoặc đã hết hạn");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);

        userRepository.save(user);

        // Gửi email thông báo mật khẩu đã được đổi
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getPersonnelId());

        return new ChangePasswordResponse(
                "Đặt lại mật khẩu thành công",
                user.getId(),
                user.getPersonnelId(),
                user.getEmail(),
                java.time.LocalDateTime.now().toString());
    }

    /**
     * Get current username from SecurityContext
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
}
