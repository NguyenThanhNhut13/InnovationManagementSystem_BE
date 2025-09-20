package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.*;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.AuthenticationService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthenticationController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    // 1. Login
    @PostMapping("/auth/login")
    @ApiMessage("Đăng nhập thành công")
    @Operation(summary = "User Login", description = "Authenticate user with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login credentials", required = true) @Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authenticationService.authenticate(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    // 2. Refresh Token
    @PostMapping("/auth/refresh")
    @ApiMessage("Refresh token thành công")
    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "Refresh token request", required = true) @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = authenticationService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    // 3. Logout
    @PostMapping("/auth/logout")
    @ApiMessage("Đăng xuất thành công")
    @Operation(summary = "User Logout", description = "Logout user and invalidate tokens")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Void> logout(
            @Parameter(description = "Authorization header with Bearer token", required = true) @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(description = "Logout request with refresh token", required = true) @Valid @RequestBody LogoutRequest logoutRequest) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IdInvalidException("Invalid authorization header");
        }

        String accessToken = authorizationHeader.substring(7);
        String refreshToken = logoutRequest.getRefreshToken();

        authenticationService.logout(accessToken, refreshToken);
        return ResponseEntity.ok().build();
    }

    // 4. Change Password
    @PostMapping("/auth/change-password")
    @ApiMessage("Đổi mật khẩu thành công")
    @Operation(summary = "Change Password", description = "Change user password with current password verification")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(schema = @Schema(implementation = ChangePasswordResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or current password")
    })
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @Parameter(description = "Change password request", required = true) @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        ChangePasswordResponse response = authenticationService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(response);
    }

    // 5. Quên mật khẩu - Gửi OTP (Public API)
    @PostMapping("/auth/forgot-password")
    @ApiMessage("OTP đã được gửi đến email của bạn")
    @Operation(summary = "Forgot Password", description = "Send OTP to email for password reset using personnel ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully", content = @Content(schema = @Schema(implementation = OtpResponse.class))),
            @ApiResponse(responseCode = "404", description = "Personnel ID not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Parameter(description = "Email for password reset", required = true) @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {

        ForgotPasswordResponse response = authenticationService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok(response);
    }

    // 6. Reset mật khẩu với OTP (Public API)
    @PostMapping("/auth/reset-password")
    @ApiMessage("Đặt lại mật khẩu thành công")
    @Operation(summary = "Reset Password", description = "Reset password using OTP verification with personnel ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully", content = @Content(schema = @Schema(implementation = ChangePasswordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid OTP or request data"),
            @ApiResponse(responseCode = "404", description = "Personnel ID not found")
    })
    public ResponseEntity<ChangePasswordResponse> resetPassword(
            @Parameter(description = "Reset password request with OTP and personnel ID", required = true) @Valid @RequestBody ResetPasswordWithOtpRequest resetPasswordRequest) {

        ChangePasswordResponse response = authenticationService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok(response);
    }

    // 7. Get Current User Profile
    @GetMapping("/auth/me")
    @ApiMessage("Lấy thông tin người dùng hiện tại thành công")
    @Operation(summary = "Get Current User Profile", description = "Get current authenticated user's profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserResponse());
    }

}
