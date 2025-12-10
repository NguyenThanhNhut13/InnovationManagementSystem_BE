package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserImportResponse;
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
import java.util.List;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

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

        // 3. Gán Role To User bằng roleName
        @PostMapping("/users/{userId}/roles/by-name/{roleName}")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_KHOA')")
        @ApiMessage("Gán vai trò cho người dùng thành công")
        @Operation(summary = "Assign Role to User by Role Name", description = "Assign a role to a user using role name instead of role ID")
        public ResponseEntity<UserRoleResponse> assignRoleToUserByRoleName(
                        @PathVariable String userId,
                        @PathVariable UserRoleEnum roleName) {
                return ResponseEntity.ok(userService.assignRoleToUserByRoleName(userId, roleName));
        }

        // 4. Xóa Role From User bằng roleName
        @DeleteMapping("/users/{userId}/roles/by-name/{roleName}")
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_QLKH_HTQT', 'QUAN_TRI_VIEN_HE_THONG', 'TRUONG_KHOA', 'QUAN_TRI_VIEN_KHOA')")
        @ApiMessage("Xóa vai trò khỏi người dùng thành công")
        @Operation(summary = "Remove Role from User by Role Name", description = "Remove a role from a user using role name instead of role ID")
        public ResponseEntity<Void> removeRoleFromUserByRoleName(
                        @PathVariable String userId,
                        @PathVariable UserRoleEnum roleName) {
                userService.removeRoleFromUserByRoleName(userId, roleName);
                return ResponseEntity.ok().build();
        }

        // 5. Tìm kiếm Users By Full Name or Personnel ID
        @GetMapping("/users/search")
        @ApiMessage("Tìm kiếm người dùng thành công")
        public ResponseEntity<ResultPaginationDTO> searchUsers(@RequestParam String searchTerm, Pageable pageable) {
                return ResponseEntity.ok(userService.searchUsersByFullNameOrPersonnelId(searchTerm,
                                pageable));
        }

        // 6. Lấy danh sách Users theo khoa hiện tại và vai trò
        @GetMapping("/users/current-department/role/{roleName}")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'QUAN_TRI_VIEN_KHOA')")
        @ApiMessage("Lấy danh sách người dùng theo khoa hiện tại và vai trò thành công")
        @Operation(summary = "Get Users by Current Department and Role", description = "Get list of users in current user's department with specified role")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
        })
        public ResponseEntity<List<UserResponse>> getUsersByCurrentDepartmentAndRole(
                        @Parameter(description = "Role name to filter users", required = true) @PathVariable UserRoleEnum roleName) {
                List<UserResponse> users = userService.getUsersByCurrentDepartmentAndRole(roleName);
                return ResponseEntity.ok(users);
        }

        // 7. Lấy danh sách tất cả Users trong khoa hiện tại
        @GetMapping("/users/current-department")
        @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'QUAN_TRI_VIEN_KHOA')")
        @ApiMessage("Lấy danh sách tất cả người dùng trong khoa hiện tại thành công")
        @Operation(summary = "Get All Users by Current Department", description = "Get list of all users in current user's department")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
        })
        public ResponseEntity<List<UserResponse>> getAllUsersByCurrentDepartment() {
                List<UserResponse> users = userService.getAllUsersByCurrentDepartment();
                return ResponseEntity.ok(users);
        }

        // 8. Import users từ file Excel
        @PostMapping(value = "/users/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('QUAN_TRI_VIEN_HE_THONG', 'QUAN_TRI_VIEN_QLKH_HTQT')")
        @ApiMessage("Import nhân viên từ file Excel thành công")
        @Operation(summary = "Import Users from Excel", description = "Import users from Excel file (.xlsx). Columns: personnel_id, full_name, email, date_of_birth, qualification, department_code, role (GIANG_VIEN/TRUONG_KHOA). Password defaults to personnel_id. Each department can only have one TRUONG_KHOA.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Users imported successfully", content = @Content(schema = @Schema(implementation = UserImportResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid file format or data")
        })
        public ResponseEntity<UserImportResponse> importUsers(
                        @Parameter(description = "Excel file (.xlsx) with user data") @RequestParam("file") MultipartFile file) {
                UserImportResponse response = userService.importUsersFromExcel(file);
                return ResponseEntity.ok(response);
        }

        // 9. Lấy danh sách tất cả Users (chỉ cho admin hệ thống)
        @GetMapping("/users")
        @PreAuthorize("hasRole('QUAN_TRI_VIEN_HE_THONG')")
        @ApiMessage("Lấy danh sách người dùng thành công")
        @Operation(summary = "Get All Users", description = "Get paginated list of all users. Only accessible by system administrators. Supports optional search by full name or personnel ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = ResultPaginationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Only system administrators can access")
        })
        public ResponseEntity<ResultPaginationDTO> getAllUsers(
                        @Parameter(description = "Search term (optional) - search by full name or personnel ID") @RequestParam(required = false) String searchTerm,
                        Pageable pageable) {
                ResultPaginationDTO result = userService.getAllUsers(pageable, searchTerm);
                return ResponseEntity.ok(result);
        }

}
