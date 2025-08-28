package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Create User
    @PostMapping("/users")
    @ApiMessage("Tạo người dùng thành công")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    // 2. Get All Users
    @GetMapping("/users")
    @ApiMessage("Lấy danh sách người dùng thành công")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersWithPagination(spec, pageable));
    }

    // 3. Get User By Id
    @GetMapping("/users/{id}")
    @ApiMessage("Lấy thông tin người dùng thành công")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // 4. Update User
    @PutMapping("/users/{id}")
    @ApiMessage("Cập nhật thông tin người dùng thành công")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
            @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    // 5. Get Users By Status With Pagination
    @GetMapping("/users/status")
    @ApiMessage("Lấy danh sách người dùng theo status thành công")
    public ResponseEntity<ResultPaginationDTO> getUsersByStatusWithPagination(
            @Filter @RequestParam UserStatusEnum status,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByStatusWithPagination(status, pageable));
    }

    // 6. Assign Role To User
    @PostMapping("/users/{userId}/roles/{roleId}")
    @ApiMessage("Gán vai trò cho người dùng thành công")
    public ResponseEntity<UserRoleResponse> assignRoleToUser(@PathVariable String userId, @PathVariable String roleId) {
        return ResponseEntity.ok(userService.assignRoleToUser(userId, roleId));
    }

    // 7. Remove Role From User
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @ApiMessage("Xóa vai trò khỏi người dùng thành công")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable String userId, @PathVariable String roleId) {
        userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    // 8. Get Users By Role With Pagination
    @GetMapping("/roles/{roleId}/users")
    @ApiMessage("Lấy danh sách người dùng theo vai trò thành công")
    public ResponseEntity<ResultPaginationDTO> getUsersByRoleWithPagination(@Filter @PathVariable String roleId,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByRoleWithPagination(roleId, pageable));
    }

    // 9. Get Users By Department With Pagination
    @GetMapping("/users/departments/{departmentId}/users")
    @ApiMessage("Lấy danh sách người dùng theo phòng ban thành công")
    public ResponseEntity<ResultPaginationDTO> getUsersByDepartmentWithPagination(
            @Filter @PathVariable String departmentId,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByDepartmentWithPagination(departmentId, pageable));
    }

    // 10. Search Users By Full Name, Email or Personnel ID
    @GetMapping("/users/search")
    @ApiMessage("Tìm kiếm người dùng thành công")
    public ResponseEntity<ResultPaginationDTO> searchUsers(
            @RequestParam String searchTerm,
            Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsersByFullNameOrEmailOrPersonnelId(searchTerm, pageable));
    }
}
