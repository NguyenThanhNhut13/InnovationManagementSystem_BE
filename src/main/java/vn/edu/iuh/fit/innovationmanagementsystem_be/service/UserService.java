package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserCreateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserUpdateRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy role chính (primary role) của user
     */
    public UserRoleEnum getPrimaryRole(String userId) {
        List<UserRole> userRoles = userRoleRepository.findPrimaryRoleByUserId(userId);
        if (userRoles.isEmpty()) {
            throw new RuntimeException("User không có role nào");
        }
        return userRoles.get(0).getRole().getRoleName();
    }

    /**
     * Lấy tất cả roles của user
     */
    public List<UserRoleEnum> getAllRoles(String userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .map(ur -> ur.getRole().getRoleName())
                .collect(Collectors.toList());
    }

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
            List<UserRole> userRoles = userRoleRepository.findByRoleName(role);
            return userRoles.stream()
                    .map(ur -> mapToResponse(ur.getUser()))
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
        UserRoleEnum roleEnum;
        try {
            roleEnum = UserRoleEnum.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ: " + request.getRole());
        }

        // Get role from database
        Role role = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleEnum));

        // Create user
        User user = new User();
        user.setPersonnelId(request.getPersonnelId());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDepartment(department);

        User savedUser = userRepository.save(user);

        // Create UserRole relationship
        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

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
        UserRoleEnum roleEnum;
        try {
            roleEnum = UserRoleEnum.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ: " + request.getRole());
        }

        // Get role from database
        Role newRole = roleRepository.findByRoleName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleEnum));

        // Update user
        existingUser.setFullName(request.getFullName());
        existingUser.setEmail(request.getEmail());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        existingUser.setDepartment(department);

        User updatedUser = userRepository.save(existingUser);

        // Update UserRole relationship - delete old and create new
        userRoleRepository.deleteByUserId(id);
        UserRole userRole = new UserRole();
        userRole.setUser(updatedUser);
        userRole.setRole(newRole);
        userRoleRepository.save(userRole);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        // Business validation before delete
        validateUserDelete(user);

        // Delete UserRole relationships first
        userRoleRepository.deleteByUserId(id);

        // Then delete user
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
}
