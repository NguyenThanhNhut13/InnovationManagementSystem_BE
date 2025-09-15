package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    boolean existsByUserIdAndRoleId(String userId, String roleId);

    void deleteByUserIdAndRoleId(String userId, String roleId);

    Page<UserRole> findByRoleId(String roleId, Pageable pageable);

    boolean existsByRoleNameAndUserDepartmentId(UserRoleEnum roleName, String departmentId);

}