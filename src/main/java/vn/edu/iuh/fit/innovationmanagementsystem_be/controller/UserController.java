package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.annotation.ApiMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    // 2. Get All Users
    @GetMapping("/users")
    @ApiMessage("Lấy danh sách người dùng thành công")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(spec, pageable));
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
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id, @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    // 5. Get Users By Status With Pagination
    @GetMapping("/users/status")
    @ApiMessage("Lấy danh sách người dùng theo status thành công")
    public ResponseEntity<ResultPaginationDTO> getUsersByStatusWithPagination(@RequestParam UserStatusEnum status,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByStatusWithPagination(status, pageable));
    }

}
