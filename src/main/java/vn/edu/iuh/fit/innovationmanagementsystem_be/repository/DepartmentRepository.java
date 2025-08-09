package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    // Tìm department theo tên
    Optional<Department> findByDepartmentName(String departmentName);

    // Tìm department theo mã
    Optional<Department> findByDepartmentCode(String departmentCode);

    // Kiểm tra tên department đã tồn tại chưa
    boolean existsByDepartmentName(String departmentName);

    // Kiểm tra mã department đã tồn tại chưa
    boolean existsByDepartmentCode(String departmentCode);

    // Tìm department theo tên (tìm kiếm mờ)
    List<Department> findByDepartmentNameContainingIgnoreCase(String departmentName);

    // Đếm số user trong department
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    long countUsersByDepartmentId(@Param("departmentId") String departmentId);

    // Đếm số innovation trong department
    @Query("SELECT COUNT(i) FROM Innovation i WHERE i.department.id = :departmentId")
    long countInnovationsByDepartmentId(@Param("departmentId") String departmentId);
}
