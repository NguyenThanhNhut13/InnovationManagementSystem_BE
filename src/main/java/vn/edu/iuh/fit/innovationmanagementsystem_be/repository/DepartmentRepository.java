package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String>, JpaSpecificationExecutor<Department> {

    Optional<Department> findByDepartmentCode(String departmentCode);

    boolean existsByDepartmentCode(String departmentCode);

    Optional<Department> findByDepartmentName(String departmentName);

    // Thêm method tìm kiếm có phân trang
    @Query("SELECT d FROM Department d WHERE d.departmentCode LIKE %:keyword% OR d.departmentName LIKE %:keyword%")
    Page<Department> findByCodeOrNameContainingWithPagination(@Param("keyword") String keyword, Pageable pageable);

    // Tìm users trong department
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId")
    Page<User> findUsersByDepartmentId(@Param("departmentId") String departmentId, Pageable pageable);

    // Tìm users đang hoạt động trong department
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.status = 'ACTIVE'")
    Page<User> findActiveUsersByDepartmentId(@Param("departmentId") String departmentId, Pageable pageable);

    // Tìm users không hoạt động trong department
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.status != 'ACTIVE'")
    Page<User> findInactiveUsersByDepartmentId(@Param("departmentId") String departmentId, Pageable pageable);

    // Chỉ lấy departments đang hoạt động
    @Query("SELECT d FROM Department d WHERE d.isActive = true")
    Page<Department> findActiveDepartments(Pageable pageable);

    // Tìm kiếm chỉ trong departments đang hoạt động
    @Query("SELECT d FROM Department d WHERE d.isActive = true AND (d.departmentCode LIKE %:keyword% OR d.departmentName LIKE %:keyword%)")
    Page<Department> findActiveDepartmentsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Kiểm tra department có đang hoạt động không
    @Query("SELECT d.isActive FROM Department d WHERE d.id = :departmentId")
    Optional<Boolean> isDepartmentActive(@Param("departmentId") String departmentId);

}
