package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChangePasswordRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.LoginRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.LogoutRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RegisterRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.TokenRefreshRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ApiResponse;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.LoginResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TokenRefreshResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Đăng ký thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        try {
            TokenRefreshResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Làm mới token thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token không hợp lệ"));
        }

        // TODO: Extract userId from JWT token
        // For now, we'll need to get userId from request or token
        String userId = "temp-user-id"; // This should be extracted from JWT

        String result = authService.changePassword(userId, request);
        if (result.contains("thành công")) {
            return ResponseEntity.ok(ApiResponse.success(result, result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(result));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token không hợp lệ"));
        }

        // TODO: Extract userId from JWT token
        String userId = "temp-user-id"; // This should be extracted from JWT

        UserResponse userResponse = authService.getUserProfile(userId);
        if (userResponse != null) {
            return ResponseEntity.ok(ApiResponse.success(userResponse, "Lấy thông tin profile thành công"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy thông tin người dùng"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", "Đăng xuất thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<String>> logoutAllDevices(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token không hợp lệ"));
        }

        // TODO: Extract userId from JWT token
        String userId = "temp-user-id"; // This should be extracted from JWT

        try {
            authService.logoutAllDevices(userId);
            return ResponseEntity.ok(ApiResponse.success("Đăng xuất tất cả thiết bị thành công",
                    "Đăng xuất tất cả thiết bị thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
