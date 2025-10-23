package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import java.util.ArrayList;

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
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserSignatureProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UpdateProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.UserRoleResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;
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

    // 1. Tạo User
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
        request.setPathUrl(null);
        this.userSignatureProfileService.createUserSignatureProfile(request);

        return userMapper.toUserResponse(user);
    }

    // 6. Gán Role To User
    public UserRoleResponse assignRoleToUser(@NonNull String userId, @NonNull String roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại với ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IdInvalidException("Vai trò không tồn tại với ID: " + roleId));

        if (this.userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new IdInvalidException("User đã có role này");
        }

        // Kiểm tra ràng buộc: mỗi phòng ban chỉ được có 1 role TRUONG_KHOA
        if (role.getRoleName() == UserRoleEnum.TRUONG_KHOA) {
            boolean departmentHasTruongKhoa = userRoleRepository.existsByRoleRoleNameAndUserDepartmentId(
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

    // 7. Xóa Role From User
    public void removeRoleFromUser(@NonNull String userId, @NonNull String roleId) {

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

    // 11. Lấy Current User từ JWT Token
    public User getCurrentUser() {
        try {
            // Lấy JWT token từ request header
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null) {
                throw new IdInvalidException("Không thể lấy thông tin request");
            }

            HttpServletRequest request = attributes.getRequest();

            // Kiểm tra nếu user đã được cache trong request
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

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByPersonnelId(userId);
            }
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(userId);
            }

            User user = userOpt
                    .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng hiện tại với ID: " + userId));

            request.setAttribute("currentUser", user);

            return user;

        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi lấy thông tin người dùng hiện tại: " + e.getMessage());
        }
    }

    // 12. Lấy Current User ID
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
            String personnelId = jwtTokenUtil.extractUsername(token);

            if (personnelId == null) {
                throw new IdInvalidException("Không thể extract personnel ID từ token");
            }

            Optional<User> userOpt = userRepository.findByPersonnelId(personnelId);
            if (userOpt.isEmpty()) {
                throw new IdInvalidException("Không tìm thấy người dùng với personnel ID: " + personnelId);
            }

            return userOpt.get().getId();

        } catch (Exception e) {
            throw new IdInvalidException("Lỗi khi lấy user ID: " + e.getMessage());
        }
    }

    // 13. Kiểm tra nếu current user là owner của innovation
    public boolean isOwnerOfInnovation(String innovationUserId) {
        User currentUser = getCurrentUser();
        return currentUser.getId().equals(innovationUserId);
    }

    // 15. Lấy Current User Response
    public UserResponse getCurrentUserResponse() {
        User currentUser = getCurrentUser();
        return userMapper.toUserResponse(currentUser);
    }

    // 16. Cập nhật Profile của Current User
    public UserResponse updateCurrentUserProfile(@NonNull UpdateProfileRequest updateProfileRequest) {
        User currentUser = getCurrentUser();

        if (updateProfileRequest.getFullName() != null && !updateProfileRequest.getFullName().trim().isEmpty()) {
            currentUser.setFullName(updateProfileRequest.getFullName().trim());
        }

        if (updateProfileRequest.getEmail() != null && !updateProfileRequest.getEmail().trim().isEmpty()) {
            String newEmail = updateProfileRequest.getEmail().trim();
            if (!currentUser.getEmail().equals(newEmail)) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new IdInvalidException("Email đã tồn tại trong hệ thống");
                }
                currentUser.setEmail(newEmail);
            }
        }

        if (updateProfileRequest.getPhoneNumber() != null && !updateProfileRequest.getPhoneNumber().trim().isEmpty()) {
            String newPhoneNumber = updateProfileRequest.getPhoneNumber().trim();
            if (!currentUser.getPhoneNumber().equals(newPhoneNumber)) {
                if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
                    throw new IdInvalidException("Số điện thoại đã tồn tại trong hệ thống");
                }
                currentUser.setPhoneNumber(newPhoneNumber);
            }
        }

        if (updateProfileRequest.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(updateProfileRequest.getDateOfBirth());
        }

        if (updateProfileRequest.getQualification() != null) {
            currentUser.setQualification(updateProfileRequest.getQualification().trim());
        }

        if (updateProfileRequest.getTitle() != null) {
            currentUser.setTitle(updateProfileRequest.getTitle().trim());
        }

        userRepository.save(currentUser);
        return userMapper.toUserResponse(currentUser);
    }

    /**
     * Gán Default Role To User (GIANG_VIEN)
     */
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
}
