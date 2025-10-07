package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    // 2. Get All Users
    @GetMapping("/users")
    @ApiMessage("Lấy danh sách người dùng thành công")
    @Operation(summary = "Get All Users", description = "Get paginated list of all users with filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Parameter(description = "Filter specification for users") @Filter Specification<User> spec,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersWithPagination(spec, pageable));
    }

    // 3. Lấy User By Id
    @GetMapping("/users/{id}")
    @ApiMessage("Lấy thông tin người dùng thành công")
    @Operation(summary = "Get User by ID", description = "Get user details by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // 4. Cập nhật User
    @PutMapping("/users/{id}")
    @ApiMessage("Cập nhật thông tin người dùng thành công")
    @Operation(summary = "Update User", description = "Update user information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable String id,
            @Parameter(description = "Updated user information", required = true) @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    // 5. Lấy Users By Status với Pagination
    @GetMapping("/users/status")
    @ApiMessage("Lấy danh sách người dùng theo status thành công")
    public ResponseEntity<ResultPaginationDTO> getUsersByStatusWithPagination(
            @Filter @RequestParam UserStatusEnum status,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByStatusWithPagination(status, pageable));
    }

    // 6. Gán Role To User
    @PostMapping("/users/{userId}/roles/{roleId}")
    @ApiMessage("Gán vai trò cho người dùng thành công")
    public ResponseEntity<UserRoleResponse> assignRoleToUser(@PathVariable String userId, @PathVariable String roleId) {
        return ResponseEntity.ok(userService.assignRoleToUser(userId, roleId));
    }

    // 7. Xóa Role From User
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @ApiMessage("Xóa vai trò khỏi người dùng thành công")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable String userId, @PathVariable String roleId) {
        userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    // 8. Lấy Users By Role với Pagination
    @GetMapping("/roles/{roleId}/users")
    @ApiMessage("Lấy danh sách người dùng theo vai trò thành công")
    public ResponseEntity<ResultPaginationDTO> getUsersByRoleWithPagination(@Filter @PathVariable String roleId,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByRoleWithPagination(roleId, pageable));
    }

    // 9. Lấy Users By Department với Pagination
    @GetMapping("/users/departments/{departmentId}/users")
    @ApiMessage("Lấy danh sách người dùng theo phòng ban thành công")
    public ResponseEntity<ResultPaginationDTO> getUsersByDepartmentWithPagination(
            @Filter @PathVariable String departmentId,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByDepartmentWithPagination(departmentId, pageable));
    }

    // 10. Tìm kiếm Users By Full Name, Email or Personnel ID
    @GetMapping("/users/search")
    @ApiMessage("Tìm kiếm người dùng thành công")
    public ResponseEntity<ResultPaginationDTO> searchUsers(
            @RequestParam String searchTerm,
            Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsersByFullNameOrEmailOrPersonnelId(searchTerm, pageable));
    }

}
