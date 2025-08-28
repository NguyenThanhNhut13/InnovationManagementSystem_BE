package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ChangePasswordRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.LoginRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.LogoutRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.OtpRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.RefreshTokenRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.ResetPasswordWithOtpRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.ChangePasswordResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.LoginResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.OtpResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.TokenResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.AuthenticationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // 1. Login
    @PostMapping("/auth/login")
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authenticationService.authenticate(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    // 2. Refresh Token
    @PostMapping("/auth/refresh")
    @ApiMessage("Refresh token thành công")
    public ResponseEntity<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = authenticationService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    // 3. Logout
    @PostMapping("/auth/logout")
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody LogoutRequest logoutRequest) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String accessToken = authorizationHeader.substring(7);
        String refreshToken = logoutRequest.getRefreshToken();

        authenticationService.logout(accessToken, refreshToken);
        return ResponseEntity.ok().build();
    }

    // 4. Change Password
    @PostMapping("/auth/change-password")
    @ApiMessage("Đổi mật khẩu thành công")
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        ChangePasswordResponse response = authenticationService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(response);
    }

    // 5. Quên mật khẩu - Gửi OTP (Public API)
    @PostMapping("/auth/forgot-password")
    @ApiMessage("OTP đã được gửi đến email của bạn")
    public ResponseEntity<OtpResponse> forgotPassword(
            @Valid @RequestBody OtpRequest otpRequest) {

        OtpResponse response = authenticationService.forgotPassword(otpRequest);
        return ResponseEntity.ok(response);
    }

    // 6. Reset mật khẩu với OTP (Public API)
    @PostMapping("/auth/reset-password")
    @ApiMessage("Đặt lại mật khẩu thành công")
    public ResponseEntity<ChangePasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordWithOtpRequest resetPasswordRequest) {

        ChangePasswordResponse response = authenticationService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok(response);
    }

}
