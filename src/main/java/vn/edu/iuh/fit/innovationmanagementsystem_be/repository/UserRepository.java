package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

        boolean existsByPersonnelId(String personnelId);

        boolean existsByEmail(String email);

        Optional<User> findByPersonnelId(String personnelId);

        Optional<User> findByEmail(String email);

        @Query("SELECT u FROM User u WHERE " +
                        "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.personnelId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<User> searchUsersByFullNameOrPersonnelId(@Param("searchTerm") String searchTerm, Pageable pageable);

        @Query("SELECT u FROM User u WHERE LOWER(TRIM(u.fullName)) = LOWER(TRIM(:fullName))")
        Optional<User> findByFullNameIgnoreCase(@Param("fullName") String fullName);

        @Query("SELECT DISTINCT u FROM User u " +
                        "JOIN u.userRoles ur " +
                        "JOIN ur.role r " +
                        "WHERE r.roleName = :roleName")
        List<User> findUsersByRole(
                        @Param("roleName") vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum roleName);

        @Query("SELECT u FROM User u WHERE u.department.id = :departmentId")
        List<User> findByDepartmentId(@Param("departmentId") String departmentId);

        @Query("SELECT DISTINCT u FROM User u " +
                        "JOIN u.userRoles ur " +
                        "JOIN ur.role r " +
                        "WHERE u.department.id = :departmentId " +
                        "AND r.roleName IN :roles")
        List<User> findByDepartmentIdAndRoles(
                        @Param("departmentId") String departmentId,
                        @Param("roles") List<vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum> roles);

        @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u "
                        + "JOIN u.userRoles ur "
                        + "JOIN ur.role r "
                        + "WHERE u.id = :userId AND r.roleName = :roleName")
        boolean userHasRole(@Param("userId") String userId, @Param("roleName") UserRoleEnum roleName);

}