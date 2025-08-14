package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String>, JpaSpecificationExecutor<Department> {

    // Tìm Department theo code
    Optional<Department> findByDepartmentCode(String departmentCode);

    // Kiểm tra department code đã tồn tại không
    boolean existsByDepartmentCode(String departmentCode);

    // Tìm Department theo tên
    Optional<Department> findByDepartmentName(String departmentName);

    // Tìm department theo code hoặc tên
    @Query("SELECT d FROM Department d WHERE d.departmentCode LIKE %:keyword% OR d.departmentName LIKE %:keyword%")
    List<Department> findByCodeOrNameContaining(@Param("keyword") String keyword);

}
