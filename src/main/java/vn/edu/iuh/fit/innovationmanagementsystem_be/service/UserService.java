package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import java.util.ArrayList;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.CouncilMemberRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.CouncilMemberRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.JwtTokenUtil;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.ResultPaginationDTO;
import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.Utils;
import vn.edu.iuh.fit.innovationmanagementsystem_be.mapper.UserMapper;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

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
    private final CouncilMemberRepository councilMemberRepository;
    private final CertificateRevocationService certificateRevocationService;

    public UserService(UserRepository userRepository, DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRoleRepository userRoleRepository,
            UserMapper userMapper, JwtTokenUtil jwtTokenUtil,
            UserSignatureProfileService userSignatureProfileService, CouncilMemberRepository councilMemberRepository,
            CertificateRevocationService certificateRevocationService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userMapper = userMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userSignatureProfileService = userSignatureProfileService;
        this.councilMemberRepository = councilMemberRepository;
        this.certificateRevocationService = certificateRevocationService;
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
        UserResponse response = userMapper.toUserResponse(currentUser);

        // Set isSecretary và isChairman
        setCouncilRoleFlags(currentUser, response);

        return response;
    }

    // Helper method: Get council role flags cho user
    public CouncilRoleFlags getCouncilRoleFlags(String userId) {
        List<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CouncilMember> memberships = councilMemberRepository
                .findByUserId(userId);

        boolean isSecretary = memberships.stream()
                .anyMatch(member -> member.getRole() == CouncilMemberRoleEnum.THU_KY);

        boolean isChairman = memberships.stream()
                .anyMatch(member -> member.getRole() == CouncilMemberRoleEnum.CHU_TICH);

        return new CouncilRoleFlags(isSecretary, isChairman);
    }

    // Helper method: Set isSecretary và isChairman cho user
    private void setCouncilRoleFlags(User user, UserResponse response) {
        CouncilRoleFlags flags = getCouncilRoleFlags(user.getId());
        response.setIsSecretary(flags.isSecretary());
        response.setIsChairman(flags.isChairman());
    }

    // Inner class để chứa council role flags
    public static class CouncilRoleFlags {
        private final boolean isSecretary;
        private final boolean isChairman;

        public CouncilRoleFlags(boolean isSecretary, boolean isChairman) {
            this.isSecretary = isSecretary;
            this.isChairman = isChairman;
        }

        public boolean isSecretary() {
            return isSecretary;
        }

        public boolean isChairman() {
            return isChairman;
        }
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

    // 17. Tìm kiếm Users By Full Name or Personnel ID
    public ResultPaginationDTO searchUsersByFullNameOrPersonnelId(@NonNull String searchTerm,
            @NonNull Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IdInvalidException("Từ khóa tìm kiếm không được để trống");
        }

        Page<User> userPage = userRepository.searchUsersByFullNameOrPersonnelId(searchTerm.trim(), pageable);
        Page<UserResponse> userResponsePage = userPage.map(userMapper::toUserResponse);
        return Utils.toResultPaginationDTO(userResponsePage, pageable);
    }

    // 18. Lấy danh sách Users theo khoa hiện tại và vai trò
    public List<UserResponse> getUsersByCurrentDepartmentAndRole(@NonNull UserRoleEnum roleName) {
        User currentUser = getCurrentUser();
        if (currentUser.getDepartment() == null) {
            throw new IdInvalidException("Người dùng hiện tại chưa được gán vào khoa nào");
        }

        String departmentId = currentUser.getDepartment().getId();
        List<User> users = userRepository.findByDepartmentIdAndRoles(
                departmentId,
                List.of(roleName));

        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    // 19. Lấy danh sách tất cả Users trong khoa hiện tại
    public List<UserResponse> getAllUsersByCurrentDepartment() {
        User currentUser = getCurrentUser();
        if (currentUser.getDepartment() == null) {
            throw new IdInvalidException("Người dùng hiện tại chưa được gán vào khoa nào");
        }

        String departmentId = currentUser.getDepartment().getId();
        List<User> users = userRepository.findByDepartmentId(departmentId);

        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    // 20. Cập nhật User Status (với auto revocation/restoration)
    public void updateUserStatus(String userId,
            vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("User không tồn tại"));

        vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum oldStatus = user.getStatus();

        // Cập nhật status
        user.setStatus(newStatus);
        userRepository.save(user);

        // Nếu chuyển từ ACTIVE -> INACTIVE/SUSPENDED → Thu hồi certificate
        if (oldStatus == vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum.ACTIVE
                && newStatus != vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum.ACTIVE) {
            certificateRevocationService.revokeOnStatusChange(user, newStatus);
            org.slf4j.LoggerFactory.getLogger(UserService.class).info(
                    "User {} status changed from {} to {} - Certificate revoked",
                    userId, oldStatus, newStatus);
        }

        // Nếu chuyển từ INACTIVE/SUSPENDED -> ACTIVE → Khôi phục certificate
        if (oldStatus != vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum.ACTIVE
                && newStatus == vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum.ACTIVE) {
            certificateRevocationService.restoreCertificate(userId);
            org.slf4j.LoggerFactory.getLogger(UserService.class).info(
                    "User {} reactivated - Certificate restored", userId);
        }
    }
}
