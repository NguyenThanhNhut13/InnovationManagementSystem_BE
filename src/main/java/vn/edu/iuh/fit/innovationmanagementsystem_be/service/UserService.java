package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.UserMapper;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRoleRepository userRoleRepository,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userMapper = userMapper;
    }

    // 1. Cretae User
    public UserResponse createUser(@NonNull UserRequest userRequest) {
        if (userRepository.existsByPersonnelId(userRequest.getPersonnelId())) {
            throw new IdInvalidException("Mã nhân viên đã tồn tại");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IdInvalidException("Email đã tồn tại");
        }

        Department department = departmentRepository.findById(userRequest.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));

        User user = userMapper.toUser(userRequest);
        user.setDepartment(department);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        userRepository.save(user);

        assignDefaultRoleToUser(user);

        return userMapper.toUserResponse(user);
    }

    // 2. Get All Users With Pagination
    public ResultPaginationDTO getUsersWithPagination(@NonNull Specification<User> specification,
            @NonNull Pageable pageable) {
        Page<User> users = userRepository.findAll(specification, pageable);

        Page<UserResponse> userResponses = users.map(userMapper::toUserResponse);

        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 3. Get User By Id
    public UserResponse getUserById(@NonNull String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại với ID: " + id));
        return userMapper.toUserResponse(user);
    }

    // 4. Update User
    public UserResponse updateUser(@NonNull String id, @NonNull UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại với ID: " + id));

        if (userRequest.getFullName() != null) {
            user.setFullName(userRequest.getFullName());
        }
        if (userRequest.getEmail() != null) {
            if (!user.getEmail().equals(userRequest.getEmail())
                    && userRepository.existsByEmail(userRequest.getEmail())) {
                throw new IdInvalidException("Email đã tồn tại trong hệ thống");
            }
            user.setEmail(userRequest.getEmail());
        }
        if (userRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }
        if (userRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userRequest.getDepartmentId())
                    .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));
            user.setDepartment(department);
        }
        user.setStatus(userRequest.getStatus() != null ? userRequest.getStatus() : UserStatusEnum.ACTIVE);

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    // 5. Get Users By Status With Pagination
    public ResultPaginationDTO getUsersByStatusWithPagination(@NonNull UserStatusEnum status,
            @NonNull Pageable pageable) {
        Specification<User> statusSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"),
                status);

        Page<User> userPage = userRepository.findAll(statusSpec, pageable);

        Page<UserResponse> userResponses = userPage.map(userMapper::toUserResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 6. Assign Role To User
    public UserRoleResponse assignRoleToUser(@NonNull String userId, @NonNull String roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại với ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IdInvalidException("Vai trò không tồn tại với ID: " + roleId));

        if (this.userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new IdInvalidException("User đã có role này");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        return userMapper.toUserRoleResponse(userRole);
    }

    // 7. Delete Role From User
    public void removeRoleFromUser(@NonNull String userId, @NonNull String roleId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new IdInvalidException("User không tồn tại với ID: " + userId);
        }

        // Check if role exists
        if (!roleRepository.existsById(roleId)) {
            throw new IdInvalidException("Role không tồn tại với ID: " + roleId);
        }

        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new IdInvalidException("User không có role này nên không thể xóa");
        }
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);

    }

    // 8. Get Users By Role With Pagination
    public ResultPaginationDTO getUsersByRoleWithPagination(@NonNull String roleId, @NonNull Pageable pageable) {
        Page<UserRole> userRolePage = userRoleRepository.findByRoleId(roleId, pageable);

        Page<User> userPage = userRolePage.map(UserRole::getUser);

        Page<UserResponse> userResponsePage = userPage.map(userMapper::toUserResponse);

        return Utils.toResultPaginationDTO(userResponsePage, pageable);

    }

    // Assign Default Role To User (GIANG_VIEN)
    private void assignDefaultRoleToUser(User user) {
        Role giangVienRole = roleRepository.findByRoleName(UserRoleEnum.GIANG_VIEN)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy role GIANG_VIEN"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(giangVienRole);
        userRoleRepository.save(userRole);

        if (user.getUserRoles() == null) {
            user.setUserRoles(new ArrayList<>());
        }
        user.getUserRoles().add(userRole);

    }

    // 9. Get Users By Department With Pagination
    public ResultPaginationDTO getUsersByDepartmentWithPagination(@NonNull String departmentId,
            @NonNull Pageable pageable) {
        // Check if department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new IdInvalidException("Phòng ban không tồn tại với ID: " + departmentId);
        }

        Page<User> userPage = userRepository.findByDepartmentId(departmentId, pageable);
        Page<UserResponse> userResponsePage = userPage.map(userMapper::toUserResponse);
        return Utils.toResultPaginationDTO(userResponsePage, pageable);
    }

    // 10. Search Users By Full Name, Email or Personnel ID
    public ResultPaginationDTO searchUsersByFullNameOrEmailOrPersonnelId(@NonNull String searchTerm,
            @NonNull Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IdInvalidException("Từ khóa tìm kiếm không được để trống");
        }

        Page<User> userPage = userRepository.searchUsersByFullNameOrEmailOrPersonnelId(searchTerm.trim(), pageable);
        Page<UserResponse> userResponsePage = userPage.map(userMapper::toUserResponse);
        return Utils.toResultPaginationDTO(userResponsePage, pageable);
    }

}
