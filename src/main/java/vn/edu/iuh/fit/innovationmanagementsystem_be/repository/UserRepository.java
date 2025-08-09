package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.UserRoleEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Tìm user theo email
    Optional<User> findByEmail(String email);

    // Tìm user theo personnelId
    Optional<User> findByPersonnelId(String personnelId);

    // Tìm tất cả user theo department
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId")
    List<User> findByDepartmentId(@Param("departmentId") String departmentId);

    // Tìm user theo email và password (cho login)
    Optional<User> findByEmailAndPassword(String email, String password);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    // Kiểm tra personnelId đã tồn tại chưa
    boolean existsByPersonnelId(String personnelId);

    // Tìm user theo tên đầy đủ (tìm kiếm mờ)
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:fullName%")
    List<User> findByFullNameContaining(@Param("fullName") String fullName);

    // Tìm user theo tên đầy đủ (tìm kiếm mờ, không phân biệt hoa thường)
    List<User> findByFullNameContainingIgnoreCase(String fullName);

    // Tìm user theo email hoặc mã nhân viên
    @Query("SELECT u FROM User u WHERE u.email = :emailOrPersonnelId OR u.personnelId = :emailOrPersonnelId")
    Optional<User> findByEmailOrPersonnelId(@Param("emailOrPersonnelId") String emailOrPersonnelId);
}
