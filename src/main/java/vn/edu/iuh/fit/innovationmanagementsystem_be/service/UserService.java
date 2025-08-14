package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.DepartmentResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.UserRoleResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // 1. Cretae User
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByPersonnelId(userRequest.getPersonnelId())) {
            throw new IdInvalidException("Mã nhân viên đã tồn tại");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IdInvalidException("Email đã tồn tại");
        }

        Department department = departmentRepository.findById(userRequest.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại"));

        User user = toUser(userRequest);
        user.setDepartment(department);
        userRepository.save(user);

        assignDefaultRoleToUser(user);

        return toUserResponse(user);
    }

    // 2. Get All Users
    public ResultPaginationDTO getAllUsers(Specification<User> specification, Pageable pageable) {
        Page<User> users = userRepository.findAll(specification, pageable);

        Page<UserResponse> userResponses = users.map(this::toUserResponse);

        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 3. Get User By Id
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại với ID: " + id));
        return toUserResponse(user);
    }

    // 4. Update User
    public UserResponse updateUser(String id, UserRequest userRequest) {
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
        return toUserResponse(user);
    }

    // 5. Get Users By Status With Pagination
    public ResultPaginationDTO getUsersByStatusWithPagination(UserStatusEnum status, Pageable pageable) {
        Specification<User> statusSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"),
                status);

        Page<User> userPage = userRepository.findAll(statusSpec, pageable);

        Page<UserResponse> userResponses = userPage.map(this::toUserResponse);
        return Utils.toResultPaginationDTO(userResponses, pageable);
    }

    // 6. Assign Role To User
    public UserRoleResponse assignRoleToUser(String userId, String roleId) {
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
        return toUserRoleResponse(userRole);
    }

    // 7. Delete Role From User
    public void removeRoleFromUser(String userId, String roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("User không tồn tại với ID: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IdInvalidException("Role không tồn tại với ID: " + roleId));

        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new IdInvalidException("User không có role này nên không thể xóa");
        }
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);

    }

    // 8. Get Users By Role With Pagination
    public ResultPaginationDTO getUsersByRoleWithPagination(String roleId, Pageable pageable) {
        Page<UserRole> userRolePage = userRoleRepository.findByRoleId(roleId, pageable);

        Page<User> userPage = userRolePage.map(UserRole::getUser);

        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);

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
    public ResultPaginationDTO getUsersByDepartmentWithPagination(String departmentId, Pageable pageable) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại với ID: " + departmentId));

        Page<User> userPage = userRepository.findByDepartmentId(departmentId, pageable);
        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);
        return Utils.toResultPaginationDTO(userResponsePage, pageable);
    }

    // 10. Search Users By Full Name, Email or Personnel ID
    public ResultPaginationDTO searchUsersByFullNameOrEmailOrPersonnelId(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IdInvalidException("Từ khóa tìm kiếm không được để trống");
        }

        Page<User> userPage = userRepository.searchUsersByFullNameOrEmailOrPersonnelId(searchTerm.trim(), pageable);
        Page<UserResponse> userResponsePage = userPage.map(this::toUserResponse);
        return Utils.toResultPaginationDTO(userResponsePage, pageable);
    }

    // Mapper
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getPersonnelId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null,
                user.getInnovations() != null ? user.getInnovations().size() : 0,
                user.getCoInnovations() != null ? user.getCoInnovations().size() : 0,
                user.getUserRoles() != null
                        ? user.getUserRoles().stream()
                                .map(userRole -> userRole.getRole() != null && userRole.getRole().getRoleName() != null
                                        ? userRole.getRole().getRoleName().name()
                                        : null)
                                .collect(Collectors.toList())
                        : Collections.emptyList(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private User toUser(UserRequest userRequest) {
        User user = new User();
        user.setPersonnelId(userRequest.getPersonnelId());
        user.setFullName(userRequest.getFullName());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setStatus(userRequest.getStatus() != null ? userRequest.getStatus() : UserStatusEnum.ACTIVE);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setDepartment(departmentRepository.findById(userRequest.getDepartmentId())
                .orElseThrow(() -> new IdInvalidException("Phòng ban không tồn tại")));
        return user;
    }

    private UserRoleResponse toUserRoleResponse(UserRole userRole) {
        return new UserRoleResponse(
                userRole.getId(),
                userRole.getRole().getId(),
                userRole.getRole().getRoleName().name(),
                userRole.getUser().getId(),
                userRole.getUser().getFullName());
    }

    private DepartmentResponse toDepartmentResponse(Object[] result) {
        DepartmentResponse departmentResponse = new DepartmentResponse();
        departmentResponse.setDepartmentId((String) result[0]);
        departmentResponse.setDepartmentName((String) result[1]);
        departmentResponse.setDepartmentCode((String) result[2]);
        departmentResponse.setTotalUsers((Long) result[3]);
        departmentResponse.setActiveUsers((Long) result[4]);
        departmentResponse.setInactiveUsers((Long) result[5]);
        departmentResponse.setSuspendedUsers((Long) result[6]);
        return departmentResponse;
    }

}
