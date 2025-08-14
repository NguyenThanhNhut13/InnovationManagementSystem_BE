package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserStatusEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    // Tìm user theo personnel ID
    Optional<User> findByPersonnelId(String personnelId);

    // Kiểm tra personnel ID có tồn tại không
    boolean existsByPersonnelId(String personnelId);

    // Tìm user theo email
    Optional<User> findByEmail(String email);

    // Kiểm tra email có tồn tại không
    boolean existsByEmail(String email);

    // Tìm user theo tên
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    // Tìm user theo department
    List<User> findByDepartmentId(String departmentId);

    // Tìm user theo personnel ID hoặc tên hoặc email
    @Query("SELECT u FROM User u WHERE u.personnelId LIKE %:keyword% OR u.fullName LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> findByPersonnelIdOrFullNameOrEmailContaining(@Param("keyword") String keyword);

    // Đếm số user theo department
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") String departmentId);

    // Tìm user theo status
    List<User> findByStatus(UserStatusEnum status);
}