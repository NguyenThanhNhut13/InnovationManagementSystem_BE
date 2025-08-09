package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChangePasswordRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.LoginRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RegisterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TokenRefreshRequest;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.LoginResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TokenRefreshResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmailOrPersonnelId(),
                            request.getPassword()));

            Optional<User> userOpt = userRepository.findByEmailOrPersonnelId(request.getEmailOrPersonnelId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Tạo access token (JWT với RSA-256)
                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

                // Tạo refresh token (UUID lưu trong Redis)
                String refreshToken = refreshTokenService.createRefreshToken(user.getId());

                return new LoginResponse(
                        accessToken,
                        refreshToken,
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getDepartment().getDepartmentName(),
                        jwtExpiration / 1000 // Convert milliseconds to seconds
                );
            } else {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }
        } catch (Exception e) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }
    }

    public LoginResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Kiểm tra personnelId đã tồn tại
        if (userRepository.existsByPersonnelId(request.getPersonnelId())) {
            throw new RuntimeException("Mã nhân viên đã tồn tại");
        }

        // Tìm department
        Optional<Department> departmentOpt = departmentRepository.findById(request.getDepartmentId());
        if (departmentOpt.isEmpty()) {
            throw new RuntimeException("Khoa/Viện không tồn tại");
        }

        // Chuyển đổi role string thành enum
        UserRoleEnum role;
        try {
            role = UserRoleEnum.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ");
        }

        // Tạo user mới
        User user = new User();
        user.setPersonnelId(request.getPersonnelId());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setDepartment(departmentOpt.get());

        User savedUser = userRepository.save(user);

        // Tạo token
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getDepartment().getDepartmentName(),
                jwtExpiration / 1000 // Convert milliseconds to seconds
        );
    }

    public String changePassword(String userId, ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "Người dùng không tồn tại";
        }

        User user = userOpt.get();

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return "Mật khẩu cũ không đúng";
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return "Mật khẩu mới và xác nhận mật khẩu không khớp";
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Đổi mật khẩu thành công";
    }

    public UserResponse getUserProfile(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new UserResponse(
                    user.getId(),
                    user.getPersonnelId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole(),
                    user.getDepartment().getId(),
                    user.getDepartment().getDepartmentName(),
                    user.getCreatedAt(),
                    user.getUpdatedAt());
        }
        return null;
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        String userId = refreshTokenService.validateRefreshToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Get user info
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        User user = userOpt.get();

        // Create new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        // Rotate refresh token (create new, delete old)
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        return new TokenRefreshResponse(
                newAccessToken,
                newRefreshToken,
                jwtExpiration / 1000 // Convert milliseconds to seconds
        );
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
    }

    public void logoutAllDevices(String userId) {
        refreshTokenService.deleteAllRefreshTokensForUser(userId);
    }
}
