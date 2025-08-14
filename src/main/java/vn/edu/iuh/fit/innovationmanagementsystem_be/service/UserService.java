package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.dto.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
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
                                .collect(java.util.stream.Collectors.toList())
                        : java.util.Collections.emptyList(),
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

}
