package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.DepartmentRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.UserRoleRepository;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedDepartments();
        seedAdminUser();
        seedSampleUsers();
    }

    private void seedRoles() {
        // Kiểm tra xem đã có dữ liệu roles chưa
        if (roleRepository.count() > 0) {
            log.info("Roles already exist. Skipping role seeding.");
            return;
        }

        log.info("Seeding initial role data...");

        // Tạo tất cả roles từ enum
        UserRoleEnum[] allRoles = UserRoleEnum.values();

        for (UserRoleEnum roleEnum : allRoles) {
            Role role = new Role();
            role.setRoleName(roleEnum);
            roleRepository.save(role);
            log.info("Created role: {}", roleEnum.name());
        }

        log.info("Role data seeding completed. Total roles created: {}", allRoles.length);
    }

    private void seedDepartments() {
        // Kiểm tra xem đã có dữ liệu chưa
        if (departmentRepository.count() > 0) {
            log.info("Departments already exist. Skipping data seeding.");
            return;
        }

        log.info("Seeding initial department data...");

        List<DepartmentData> departmentDataList = Arrays.asList(
                new DepartmentData("Khoa Công nghệ Cơ khí", "CNCK"),
                new DepartmentData("Khoa Công nghệ Điện", "CND"),
                new DepartmentData("Khoa Công nghệ Điện tử", "CNDT"),
                new DepartmentData("Khoa Công nghệ Động Lực", "CNDL"),
                new DepartmentData("Khoa Công nghệ Hóa học", "CNHH"),
                new DepartmentData("Khoa Công nghệ May - Thời trang", "CNMTT"),
                new DepartmentData("Khoa Công nghệ Nhiệt - Lạnh", "CNNL"),
                new DepartmentData("Khoa Công nghệ Thông tin", "CNTT"),
                new DepartmentData("Khoa Kế toán - Kiểm toán", "KTKT"),
                new DepartmentData("Khoa Kỹ thuật Xây dựng", "KTXD"),
                new DepartmentData("Khoa Luật", "LUAT"),
                new DepartmentData("Khoa Ngoại ngữ", "NN"),
                new DepartmentData("Khoa Quản trị Kinh doanh", "QTKD"),
                new DepartmentData("Khoa Tài chính - Ngân hàng", "TCNH"),
                new DepartmentData("Khoa Thương mại du lịch", "TMDL"),
                new DepartmentData("Viện Công nghệ Sinh học và Thực phẩm", "VCNSHTP"),
                new DepartmentData("Viện Khoa học Công nghệ và Quản lý Môi trường", "VKHCNQLMT"));

        for (DepartmentData data : departmentDataList) {
            Department department = new Department();
            department.setDepartmentName(data.name);
            department.setDepartmentCode(data.code);

            departmentRepository.save(department);
            log.info("Created department: {} - {}", data.code, data.name);
        }

        log.info("Department data seeding completed. Total departments created: {}", departmentDataList.size());
    }

    private void seedAdminUser() {
        // Kiểm tra xem admin user đã tồn tại chưa
        if (userRepository.existsByEmail("admin@iuh.edu.vn")) {
            log.info("Admin user already exists. Skipping admin user seeding.");
            return;
        }

        log.info("Creating default admin user...");

        // Lấy khoa CNTT làm khoa mặc định cho admin
        Department cnttDepartment = departmentRepository.findByDepartmentCode("CNTT")
                .orElseThrow(
                        () -> new RuntimeException("CNTT department not found. Please run department seeding first."));

        // Lấy role QUAN_TRI_VIEN
        Role adminRole = roleRepository.findByRoleName(UserRoleEnum.QUAN_TRI_VIEN)
                .orElseThrow(
                        () -> new RuntimeException("QUAN_TRI_VIEN role not found. Please run role seeding first."));

        User adminUser = new User();
        adminUser.setPersonnelId("ADMIN001");
        adminUser.setFullName("Quản trị viên Hệ thống");
        adminUser.setEmail("admin@iuh.edu.vn");
        adminUser.setPhoneNumber("0123456789");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setDepartment(cnttDepartment);

        User savedAdminUser = userRepository.save(adminUser);

        // Tạo UserRole relationship
        UserRole userRole = new UserRole();
        userRole.setUser(savedAdminUser);
        userRole.setRole(adminRole);
        userRoleRepository.save(userRole);

        log.info("Created admin user: {} - {}", savedAdminUser.getPersonnelId(), savedAdminUser.getEmail());
        log.info("Assigned role: {} to admin user", UserRoleEnum.QUAN_TRI_VIEN.name());
        log.info("Default admin credentials: admin@iuh.edu.vn / admin123");
    }

    private void seedSampleUsers() {
        // Kiểm tra xem đã có sample users chưa
        if (userRepository.count() > 1) { // > 1 vì đã có admin user
            log.info("Sample users already exist. Skipping sample user seeding.");
            return;
        }

        log.info("Creating sample users for testing...");

        // Lấy một số departments để phân bổ users
        Department cnttDept = departmentRepository.findByDepartmentCode("CNTT").orElse(null);
        Department qtkdDept = departmentRepository.findByDepartmentCode("QTKD").orElse(null);
        Department ktktDept = departmentRepository.findByDepartmentCode("KTKT").orElse(null);

        if (cnttDept == null || qtkdDept == null || ktktDept == null) {
            log.warn("Some departments not found. Skipping sample user creation.");
            return;
        }

        List<UserData> sampleUsers = Arrays.asList(
                new UserData("GV001", "Nguyễn Văn An", "nguyenvanan@iuh.edu.vn", "0901234567", UserRoleEnum.GIANG_VIEN,
                        cnttDept),
                new UserData("TK001", "Trần Thị Bình", "tranthibinh@iuh.edu.vn", "0902345678", UserRoleEnum.THU_KY_KHOA,
                        cnttDept),
                new UserData("TK002", "Lê Minh Cường", "leminhcuong@iuh.edu.vn", "0903456789", UserRoleEnum.TRUONG_KHOA,
                        cnttDept),
                new UserData("GV002", "Phạm Thị Dung", "phamthidung@iuh.edu.vn", "0904567890", UserRoleEnum.GIANG_VIEN,
                        qtkdDept),
                new UserData("TK003", "Hoàng Văn Em", "hoangvanem@iuh.edu.vn", "0905678901", UserRoleEnum.THU_KY_KHOA,
                        qtkdDept),
                new UserData("GV003", "Vũ Thị Phương", "vuthiphuong@iuh.edu.vn", "0906789012", UserRoleEnum.GIANG_VIEN,
                        ktktDept));

        for (UserData userData : sampleUsers) {
            // Lấy role từ database
            Role role = roleRepository.findByRoleName(userData.role)
                    .orElseThrow(() -> new RuntimeException(
                            "Role " + userData.role + " not found. Please run role seeding first."));

            User user = new User();
            user.setPersonnelId(userData.personnelId);
            user.setFullName(userData.fullName);
            user.setEmail(userData.email);
            user.setPhoneNumber(userData.phoneNumber);
            user.setPassword(passwordEncoder.encode("123456")); // Default password
            user.setDepartment(userData.department);

            User savedUser = userRepository.save(user);

            // Tạo UserRole relationship
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);

            log.info("Created user: {} - {} ({})", userData.personnelId, userData.fullName, userData.role);
        }

        log.info("Sample user creation completed. Default password for all sample users: 123456");
    }

    // Inner classes để hold data
    private static class DepartmentData {
        final String name;
        final String code;

        DepartmentData(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    private static class UserData {
        final String personnelId;
        final String fullName;
        final String email;
        final String phoneNumber;
        final UserRoleEnum role;
        final Department department;

        UserData(String personnelId, String fullName, String email, String phoneNumber, UserRoleEnum role,
                Department department) {
            this.personnelId = personnelId;
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.role = role;
            this.department = department;
        }
    }
}
