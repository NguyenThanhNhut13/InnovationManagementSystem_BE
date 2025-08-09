package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserCreateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserUpdateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        return mapToResponse(user);
    }

    public List<UserResponse> getUsersByRole(String roleStr) {
        try {
            UserRoleEnum role = UserRoleEnum.valueOf(roleStr.toUpperCase());
            List<User> users = userRepository.findByRole(role);
            return users.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ: " + roleStr);
        }
    }

    public List<UserResponse> getUsersByDepartment(String departmentId) {
        // Kiểm tra department tồn tại
        if (!departmentRepository.existsById(departmentId)) {
            throw new RuntimeException("Không tìm thấy khoa/viện với ID: " + departmentId);
        }

        List<User> users = userRepository.findByDepartmentId(departmentId);
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> searchUsersByName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Tên tìm kiếm không được để trống");
        }

        List<User> users = userRepository.findByFullNameContainingIgnoreCase(fullName.trim());
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse createUser(UserCreateRequest request) {
        // Validate business rules
        validateUserCreate(request);

        // Get department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy khoa/viện với ID: " + request.getDepartmentId()));

        // Parse role
        UserRoleEnum role;
        try {
            role = UserRoleEnum.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ: " + request.getRole());
        }

        // Create user
        User user = new User();
        user.setPersonnelId(request.getPersonnelId());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setDepartment(department);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        // Find existing user
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        // Validate business rules
        validateUserUpdate(id, request);

        // Get department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy khoa/viện với ID: " + request.getDepartmentId()));

        // Parse role
        UserRoleEnum role;
        try {
            role = UserRoleEnum.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ: " + request.getRole());
        }

        // Update user
        existingUser.setFullName(request.getFullName());
        existingUser.setEmail(request.getEmail());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        existingUser.setRole(role);
        existingUser.setDepartment(department);

        User updatedUser = userRepository.save(existingUser);
        return mapToResponse(updatedUser);
    }

    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        // Business validation before delete
        validateUserDelete(user);

        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPersonnelId(String personnelId) {
        return userRepository.existsByPersonnelId(personnelId);
    }

    // Private validation methods
    private void validateUserCreate(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại: " + request.getEmail());
        }

        if (userRepository.existsByPersonnelId(request.getPersonnelId())) {
            throw new RuntimeException("Mã nhân viên đã tồn tại: " + request.getPersonnelId());
        }
    }

    private void validateUserUpdate(String userId, UserUpdateRequest request) {
        // Check email uniqueness (excluding current user)
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new RuntimeException("Email đã tồn tại: " + request.getEmail());
                    }
                });
    }

    private void validateUserDelete(User user) {
        // Add business rules for deletion
        // For example: Can't delete users with active innovations, etc.
        // This can be expanded based on business requirements
    }

    // Private mapping method
    private UserResponse mapToResponse(User user) {
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
}
