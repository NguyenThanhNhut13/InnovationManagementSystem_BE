package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.dataSeeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.RoleRepository;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements DatabaseSeeder {

    private final RoleRepository roleRepository;

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
            roleRepository.deleteAll();
        }

        List<Role> roles = createDefaultRoles();
        roleRepository.saveAll(roles);

        log.info("Đã seed thành công {} {}.", roles.size(), getConfigPrefix());
    }

    @Override
    public int getOrder() {
        return 0; // Chạy đầu tiên
    }

    private boolean isDataExists() {
        return roleRepository.count() > 0;
    }

    private List<Role> createDefaultRoles() {
        return Arrays.stream(UserRoleEnum.values())
                .map(this::createRole)
                .toList();
    }

    private Role createRole(UserRoleEnum roleEnum) {
        Role role = new Role();
        role.setRoleName(roleEnum);
        return role;
    }
}