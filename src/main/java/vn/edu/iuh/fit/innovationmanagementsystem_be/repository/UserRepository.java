package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.UserRoleEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    Optional<User> findByPersonnelId(String personnelId);

    List<User> findByRole(UserRoleEnum role);

    List<User> findByDepartmentId(UUID departmentId);

    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.role = :role")
    List<User> findByDepartmentIdAndRole(@Param("departmentId") UUID departmentId, @Param("role") UserRoleEnum role);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByPersonnelId(String personnelId);
}