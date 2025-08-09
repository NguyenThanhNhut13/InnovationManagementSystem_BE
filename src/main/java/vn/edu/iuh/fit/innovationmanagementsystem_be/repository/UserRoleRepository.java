package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Role;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRole;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    // Tìm tất cả roles của một user
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId")
    List<UserRole> findByUserId(@Param("userId") String userId);

    // Tìm tất cả users có một role cụ thể
    @Query("SELECT ur FROM UserRole ur WHERE ur.role.roleName = :roleName")
    List<UserRole> findByRoleName(@Param("roleName") UserRoleEnum roleName);

    // Kiểm tra user có role cụ thể không
    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.roleName = :roleName")
    boolean existsByUserIdAndRoleName(@Param("userId") String userId, @Param("roleName") UserRoleEnum roleName);

    // Tìm UserRole cụ thể
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    Optional<UserRole> findByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") String roleId);

    // Xóa tất cả roles của user
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    void deleteByUserId(@Param("userId") String userId);

    // Xóa role cụ thể của user
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.roleName = :roleName")
    void deleteByUserIdAndRoleName(@Param("userId") String userId, @Param("roleName") UserRoleEnum roleName);

    // Đếm số users có role cụ thể
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.roleName = :roleName")
    long countByRoleName(@Param("roleName") UserRoleEnum roleName);

    // Lấy role chính của user (role đầu tiên)
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId ORDER BY ur.id ASC")
    List<UserRole> findPrimaryRoleByUserId(@Param("userId") String userId);
}
