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
    Optional<Department> findByName(String name);

    // Tìm department theo mã
    Optional<Department> findByCode(String code);

    // Kiểm tra tên department đã tồn tại chưa
    boolean existsByName(String name);

    // Kiểm tra mã department đã tồn tại chưa
    boolean existsByCode(String code);

    // Tìm department theo tên (tìm kiếm mờ)
    @Query("SELECT d FROM Department d WHERE d.name LIKE %:name%")
    List<Department> findByNameContaining(@Param("name") String name);

    // Đếm số user trong department
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    long countUsersByDepartmentId(@Param("departmentId") String departmentId);

    // Đếm số innovation trong department
    @Query("SELECT COUNT(i) FROM Innovation i WHERE i.department.id = :departmentId")
    long countInnovationsByDepartmentId(@Param("departmentId") String departmentId);
}

