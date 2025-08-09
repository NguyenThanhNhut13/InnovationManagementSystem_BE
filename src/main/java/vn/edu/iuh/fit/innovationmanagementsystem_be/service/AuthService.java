package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChangePasswordRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.LoginRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RegisterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TokenRefreshRequest;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.LoginResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.RegisterResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TokenRefreshResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Lấy role chính (primary role) của user
     */
    private UserRoleEnum getPrimaryRole(String userId) {
        return userRoleRepository.findPrimaryRoleByUserId(userId)
                .stream()
                .findFirst()
                .map(ur -> ur.getRole().getRoleName())
                .orElseThrow(() -> new RuntimeException("User không có role nào"));
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmailOrPersonnelId(),
                            request.getPassword()));

            Optional<User> userOpt = userRepository.findByEmailOrPersonnelId(request.getEmailOrPersonnelId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Lấy primary role từ UserRole table
                UserRoleEnum primaryRole = getPrimaryRole(user.getId());

                // Tạo access token (JWT với RSA-256)
                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), primaryRole);

                // Tạo refresh token (JWT lưu trong Redis whitelist)
                String refreshToken = refreshTokenService.createRefreshToken(user.getId());

                return new LoginResponse(
                        user.getId(),
                        user.getPersonnelId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        primaryRole,
                        user.getDepartment().getId(),
                        user.getDepartment().getDepartmentName(),
                        accessToken,
                        refreshToken,
                        jwtExpiration / 1000 // Convert milliseconds to seconds
                );
            } else {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }
        } catch (Exception e) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }
    }

    public RegisterResponse register(RegisterRequest request) {
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

        // Sử dụng GIANG_VIEN làm role mặc định cho tất cả user đăng ký
        UserRoleEnum roleEnum = UserRoleEnum.GIANG_VIEN;

        // Lấy role từ database
        Role role = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleEnum));

        // Tạo user mới
        User user = new User();
        user.setPersonnelId(request.getPersonnelId());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDepartment(departmentOpt.get());

        User savedUser = userRepository.save(user);

        // Tạo UserRole relationship
        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getPersonnelId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                roleEnum,
                savedUser.getDepartment().getId(),
                savedUser.getDepartment().getDepartmentName());
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
            UserRoleEnum primaryRole = getPrimaryRole(user.getId());
            return new UserResponse(
                    user.getId(),
                    user.getPersonnelId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    primaryRole,
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

        // Get primary role from UserRole table
        UserRoleEnum primaryRole = getPrimaryRole(user.getId());

        // Create new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), primaryRole);

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
