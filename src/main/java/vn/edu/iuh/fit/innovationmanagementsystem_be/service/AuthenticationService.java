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

            return buildLoginResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Thông tin đăng nhập không hợp lệ");
        } catch (Exception e) {
            throw new RuntimeException("Xác thực thất bại: " + e.getMessage());
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        try {
            String username = jwtTokenUtil.extractUsername(refreshToken);
            UserDetails userDetails = userRepository.findByPersonnelId(username)
                    .map(user -> org.springframework.security.core.userdetails.User.builder()
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
                            .build())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với mã nhân viên: " + username));

            if (jwtTokenUtil.validateToken(refreshToken, userDetails)) {
                String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
                String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

                return new TokenResponse(
                        newAccessToken,
                        newRefreshToken,
                        jwtTokenUtil.getAccessTokenExpiration());
            } else {
                throw new RuntimeException("Refresh token không hợp lệ");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi làm mới token: " + e.getMessage());
        }
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
                refreshToken,
                jwtTokenUtil.getAccessTokenExpiration());
    }
}
