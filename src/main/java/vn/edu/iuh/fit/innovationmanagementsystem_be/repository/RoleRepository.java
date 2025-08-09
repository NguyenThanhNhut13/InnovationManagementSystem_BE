package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    // Tìm role theo tên
    Optional<Role> findByRoleName(UserRoleEnum roleName);

    // Kiểm tra role có tồn tại không
    boolean existsByRoleName(UserRoleEnum roleName);
}
