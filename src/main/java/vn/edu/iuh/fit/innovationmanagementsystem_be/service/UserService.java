package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserSignatureProfile;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserSignatureProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.UserMapper;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserSignatureProfileService userSignatureProfileService;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRoleRepository userRoleRepository,
            UserMapper userMapper, JwtTokenUtil jwtTokenUtil,
            UserSignatureProfileService userSignatureProfileService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userMapper = userMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userSignatureProfileService = userSignatureProfileService;
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

        // Tạo UserSignatureProfile cho user mới
        UserSignatureProfileRequest request = new UserSignatureProfileRequest();
        request.setUserId(user.getId());
        request.setPathUrl(null); // Để null cho user mới
        this.userSignatureProfileService.createUserSignatureProfile(request);

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
        // Kiểm tra quyền: chỉ THU_KY_QLKH_HTQT được gán role
        User currentUser = getCurrentUser();
        boolean hasPermission = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.THU_KY_QLKH_HTQT);

        if (!hasPermission) {
            throw new IdInvalidException("Chỉ có THU_KY_QLKH_HTQT mới được gán vai trò cho người dùng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại với ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IdInvalidException("Vai trò không tồn tại với ID: " + roleId));

        if (this.userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new IdInvalidException("User đã có role này");
        }

        // Kiểm tra ràng buộc: mỗi phòng ban chỉ được có 1 role TRUONG_KHOA
        if (role.getRoleName() == UserRoleEnum.TRUONG_KHOA) {
            boolean departmentHasTruongKhoa = userRoleRepository.existsByRoleNameAndUserDepartmentId(
                    UserRoleEnum.TRUONG_KHOA, user.getDepartment().getId());
            if (departmentHasTruongKhoa) {
                throw new IdInvalidException("Phòng ban này đã có trưởng khoa, không thể gán thêm role TRUONG_KHOA");
            }
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        return userMapper.toUserRoleResponse(userRole);
    }

    // 7. Delete Role From User
    public void removeRoleFromUser(@NonNull String userId, @NonNull String roleId) {
        // Kiểm tra quyền: chỉ THU_KY_QLKH_HTQT được xóa role
        User currentUser = getCurrentUser();
        boolean hasPermission = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getRoleName() == UserRoleEnum.THU_KY_QLKH_HTQT);

        if (!hasPermission) {
            throw new IdInvalidException("Chỉ có THU_KY_QLKH_HTQT mới được xóa vai trò khỏi người dùng");
        }

        if (!userRepository.existsById(userId)) {
            throw new IdInvalidException("User không tồn tại với ID: " + userId);
        }

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

    // 11. Get Current User from JWT Token
    public User getCurrentUser() {
        try {
            // Get JWT token from request header
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null) {
                throw new IdInvalidException("Không thể lấy thông tin request");
            }

            HttpServletRequest request = attributes.getRequest();

            // Check if user is already cached in request
            User cachedUser = (User) request.getAttribute("currentUser");
            if (cachedUser != null) {
                return cachedUser;
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IdInvalidException("Không tìm thấy Bearer token");
            }

            String token = authHeader.substring(7);
            String userId = jwtTokenUtil.extractUsername(token);

            if (userId == null) {
                throw new IdInvalidException("Không thể extract user ID từ token");
            }

            // Try to find user by ID first (most common case)
            Optional<User> userOpt = userRepository.findById(userId);

            // If not found by ID, try to find by personnelId
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByPersonnelId(userId);
            }

            // If not found by personnelId, try to find by email
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(userId);
            }

            User user = userOpt
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng hiện tại với ID: " + userId));

            // Cache user in request scope
            request.setAttribute("currentUser", user);

            return user;

        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi lấy thông tin người dùng hiện tại: " + e.getMessage());
        }
    }

    // 12. Get Current User ID (without database query)
    public String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null) {
                throw new IdInvalidException("Không thể lấy thông tin request");
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IdInvalidException("Không tìm thấy Bearer token");
            }

            String token = authHeader.substring(7);
            return jwtTokenUtil.extractUsername(token);

        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi lấy user ID: " + e.getMessage());
        }
    }

    // 13. Check if current user is owner of innovation
    public boolean isOwnerOfInnovation(String innovationUserId) {
        User currentUser = getCurrentUser();
        // So sánh với ID trong database của current user
        return currentUser.getId().equals(innovationUserId);
    }

    // 14. Tạo UserSignatureProfile cho user hiện có (API endpoint)
    public UserSignatureProfile createUserSignatureProfileForExistingUser(String userId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy user với ID: " + userId));

        return userSignatureProfileService.createUserSignatureProfileForExistingUser(userId);
    }

}
