package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.exception.IdInvalidException;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;

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

    @Override
    public void seed() {
        if (!isEnabled()) {
            log.info("{} seeding bị tắt.", getSeederName());
            return;
        }

        log.info("Bắt đầu seed dữ liệu {}...", getConfigPrefix());

        if (!isForce() && isDataExists()) {
            log.info("Dữ liệu {} đã tồn tại, bỏ qua seeding.", getConfigPrefix());
            return;
        }

        if (isForce()) {
            log.info("Force seeding: Xóa dữ liệu cũ và tạo mới...");
            userRepository.deleteAll();
        }

        List<User> users = createDefaultUsers();
        userRepository.saveAll(users);

        log.info("Đã seed thành công {} {}.", users.size(), getConfigPrefix());
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
        user.setPersonnelId("USR_" + roleEnum.name());
        user.setFullName("User " + roleEnum.name().replace('_', ' '));
        user.setEmail(roleEnum.name().toLowerCase() + "@iuh.edu.vn");
        user.setPhoneNumber(String.format("090%07d", index));
        user.setPassword(passwordEncoder.encode("password123"));
        user.setDepartment(department);
        // Không cần set createdAt và updatedAt nữa vì User đã kế thừa từ Auditable

        List<UserRole> userRoles = new ArrayList<>();
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoles.add(userRole);
        user.setUserRoles(userRoles);

        return user;
    }

    // Giữ lại phương thức cũ làm tham khảo (không sử dụng)
    @SuppressWarnings("unused")
    private User createAdminUser() {
        Department cnttDept = departmentRepository.findByDepartmentCode("CNTT")
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy department CNTT"));
        Role adminRole = roleRepository.findByRoleName(UserRoleEnum.QUAN_TRI_VIEN)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy role QUAN_TRI_VIEN"));

        User adminUser = new User();
        adminUser.setPersonnelId("ADMIN001");
        adminUser.setFullName("Administrator");
        adminUser.setEmail("admin@iuh.edu.vn");
        adminUser.setPhoneNumber("0123456789");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setDepartment(cnttDept);
        // Không cần set createdAt và updatedAt nữa vì User đã kế thừa từ Auditable

        List<UserRole> userRoles = new ArrayList<>();
        UserRole userRole = new UserRole();
        userRole.setUser(adminUser);
        userRole.setRole(adminRole);
        userRoles.add(userRole);
        adminUser.setUserRoles(userRoles);

        return adminUser;
    }
}