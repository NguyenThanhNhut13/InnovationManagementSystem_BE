package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {

    // Tìm Role theo role name
    Optional<Role> findByRoleName(UserRoleEnum roleName);

    // Kiểm tra role name có tồn tại không
    boolean existsByRoleName(UserRoleEnum roleName);

    // Tìm tất cả roles
    List<Role> findAll();

    // Tìm roles theo danh sách role names
    List<Role> findByRoleNameIn(List<UserRoleEnum> roleNames);
}