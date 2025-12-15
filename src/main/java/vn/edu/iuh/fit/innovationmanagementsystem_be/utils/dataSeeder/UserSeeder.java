package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CertificateAuthority;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.UserSignatureProfileRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.CertificateAuthorityService;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.UserSignatureProfileService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements DatabaseSeeder {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSignatureProfileService userSignatureProfileService;
    private final CertificateAuthorityService certificateAuthorityService;

    @Override
    public void seed() {
        if (!isEnabled()) {
            log.info("{} seeding bị tắt.", getSeederName());
            return;
        }

        if (!isForce() && isDataExists()) {
            log.info("Dữ liệu {} đã tồn tại, bỏ qua seeding.", getConfigPrefix());
            return;
        }

        if (isForce()) {
            log.info("Force seeding: Xóa dữ liệu cũ và tạo mới...");
            userRepository.deleteAll();
        }

        List<User> users = createDefaultUsers();
        List<User> savedUsers = userRepository.saveAll(users);

        // Tạo UserSignatureProfile cho tất cả user
        createUserSignatureProfiles(savedUsers);

        log.info("Đã seed thành công {} {} và {} UserSignatureProfile.", users.size(), getConfigPrefix(), users.size());
    }

    @Override
    public int getOrder() {
        return 2;
    }

    private boolean isDataExists() {
        return userRepository.count() > 0;
    }

    private List<User> createDefaultUsers() {
        List<User> users = new ArrayList<>();

        // Lấy phòng ban mặc định để gán cho các user
        Department defaultDept = departmentRepository.findByDepartmentCode("CNTT")
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy department CNTT"));

        // Lấy toàn bộ role và tạo 1 user cho mỗi role
        List<Role> allRoles = roleRepository.findAll();
        int index = 1;
        for (Role role : allRoles) {
            users.add(createUserForRole(role, defaultDept, index));
            index++;
        }

        return users;
    }

    private User createUserForRole(Role role, Department department, int index) {
        UserRoleEnum roleEnum = role.getRoleName();

        User user = new User();
        user.setPersonnelId(String.format("%08d", 10000000 + index));
        user.setFullName(getFullNameByRole(roleEnum, index));
        user.setEmail(roleEnum.name().toLowerCase() + "@iuh.edu.vn");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setDepartment(department);
        user.setDateOfBirth(LocalDate.of(1980 + (index % 20), (index % 12) + 1, (index % 28) + 1));
        user.setQualification(getQualificationByRole(roleEnum));
        user.setTitle(getTitleByRole(roleEnum));

        List<UserRole> userRoles = new ArrayList<>();
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoles.add(userRole);
        user.setUserRoles(userRoles);

        return user;
    }

    // Tạo UserSignatureProfile cho tất cả user trong danh sách
    private void createUserSignatureProfiles(List<User> users) {
        log.info("Bắt đầu tạo UserSignatureProfile cho {} users...", users.size());

        for (User user : users) {
            try {
                createUserSignatureProfile(user);
                log.debug("Đã tạo UserSignatureProfile cho user: {}", user.getPersonnelId());
            } catch (Exception e) {
                log.error("Lỗi khi tạo UserSignatureProfile cho user {}: {}", user.getPersonnelId(), e.getMessage());
            }
        }

        log.info("Hoàn thành tạo UserSignatureProfile cho {} users.", users.size());
    }

    // Tạo UserSignatureProfile cho một user cụ thể
    private void createUserSignatureProfile(User user) {
        try {
            UserSignatureProfileRequest request = new UserSignatureProfileRequest();
            request.setUserId(user.getId());
            request.setPathUrl(null);

            CertificateAuthority activeCA = certificateAuthorityService.getActiveCA();
            if (activeCA != null) {
                request.setCertificateAuthorityId(activeCA.getId());
            }

            userSignatureProfileService.createUserSignatureProfile(request);
        } catch (Exception e) {
            throw new IdInvalidException(
                    "Không thể tạo hồ sơ chữ ký số cho user " + user.getPersonnelId() + ": " + e.getMessage());
        }
    }

    // Lấy tên đầy đủ dựa trên role
    private String getFullNameByRole(UserRoleEnum roleEnum, int index) {
        return switch (roleEnum) {
            case QUAN_TRI_VIEN_HE_THONG -> "Nguyễn Văn An " + index;
            case TRUONG_KHOA -> "Trần Minh Tuấn " + index;
            case QUAN_TRI_VIEN_KHOA -> "Lê Thị Hương " + index;
            case GIANG_VIEN -> "Phạm Đức Thành " + index;
            case TV_HOI_DONG_KHOA -> "Hoàng Văn Hải " + index;
            case QUAN_TRI_VIEN_QLKH_HTQT -> "Võ Thị Mai " + index;
            case TV_HOI_DONG_TRUONG -> "Đặng Quốc Việt " + index;
            case CHU_TICH_HD_TRUONG -> "Bùi Thanh Long " + index;
        };
    }

    // Lấy trình độ học vấn dựa trên role
    private String getQualificationByRole(UserRoleEnum roleEnum) {
        return switch (roleEnum) {
            case QUAN_TRI_VIEN_HE_THONG -> "Thạc Sĩ";
            case TRUONG_KHOA -> "Tiến Sĩ";
            case QUAN_TRI_VIEN_KHOA -> "Thạc Sĩ";
            case GIANG_VIEN -> "Thạc Sĩ";
            case TV_HOI_DONG_KHOA -> "Thạc Sĩ";
            case QUAN_TRI_VIEN_QLKH_HTQT -> "Thạc Sĩ";
            case TV_HOI_DONG_TRUONG -> "PGS";
            case CHU_TICH_HD_TRUONG -> "GS";
        };
    }

    // Lấy chức danh dựa trên role
    private String getTitleByRole(UserRoleEnum roleEnum) {
        return switch (roleEnum) {
            case QUAN_TRI_VIEN_HE_THONG -> "Quản trị viên hệ thống";
            case TRUONG_KHOA -> "Trưởng khoa";
            case QUAN_TRI_VIEN_KHOA -> "Quản trị viên khoa";
            case GIANG_VIEN -> "Giảng viên";
            case TV_HOI_DONG_KHOA -> "Thành viên hội đồng khoa";
            case QUAN_TRI_VIEN_QLKH_HTQT -> "Quản trị viên QLKH-HTQT";
            case TV_HOI_DONG_TRUONG -> "Thành viên hội đồng trường";
            case CHU_TICH_HD_TRUONG -> "Chủ tịch hội đồng trường";
        };
    }
}