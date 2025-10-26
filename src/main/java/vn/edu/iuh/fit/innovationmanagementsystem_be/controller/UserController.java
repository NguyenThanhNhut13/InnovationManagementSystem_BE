package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Tạo User
    @PostMapping("/users")
    @ApiMessage("Tạo người dùng thành công")
    @Operation(summary = "Create User", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "User creation request", required = true) @Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    // 2. Cập nhật Profile của User hiện tại (không cần ID)
    @PutMapping("/users/profile")
    @ApiMessage("Cập nhật thông tin cá nhân thành công")
    @Operation(summary = "Update Current User Profile", description = "Update current user's profile information without requiring ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Email or phone number already exists")
    })
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            @Parameter(description = "Updated profile information", required = true) @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(updateProfileRequest));
    }

    // 3. Gán Role To User
    @PostMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_KHOA')")
    @ApiMessage("Gán vai trò cho người dùng thành công")
    public ResponseEntity<UserRoleResponse> assignRoleToUser(@PathVariable String userId, @PathVariable String roleId) {
        return ResponseEntity.ok(userService.assignRoleToUser(userId, roleId));
    }

    // 4. Xóa Role From User
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_KHOA')")
    @ApiMessage("Xóa vai trò khỏi người dùng thành công")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable String userId, @PathVariable String roleId) {
        userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    // 5. Tìm kiếm Users By Full Name or Personnel ID
    @GetMapping("/users/search")
    @ApiMessage("Tìm kiếm người dùng thành công")
    public ResponseEntity<ResultPaginationDTO> searchUsers(@RequestParam String searchTerm, Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsersByFullNameOrPersonnelId(searchTerm,
                pageable));
    }

}
