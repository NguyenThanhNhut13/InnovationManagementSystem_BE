package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.LoginRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.LoginResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.TokenResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;
    private final RefreshTokenValidationService refreshTokenValidationService;

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

    // Đăng xuất - xóa refresh token khỏi Redis và blacklist access token
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

        // 5. Delete refresh token from Redis
        redisTokenService.deleteRefreshToken(refreshToken);

        // 6. Blacklist access token với TTL dài hơn để đảm bảo token bị vô hiệu hóa
        // Sử dụng TTL của access token + thêm 1 giờ để đảm bảo an toàn
        long accessTokenTTL = (jwtTokenUtil.getAccessTokenExpiration() / 1000) + 3600; // +1 giờ
        redisTokenService.blacklistAccessToken(accessToken, accessTokenTTL);

    }

    /**
     * Extract personnelId từ access token
     */
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
}
