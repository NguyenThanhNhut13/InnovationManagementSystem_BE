package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

        boolean existsByPersonnelId(String personnelId);

        boolean existsByEmail(String email);

        // Tìm user theo department
        @Query("SELECT u FROM User u WHERE u.department.id = :departmentId")
        Page<User> findByDepartmentId(@Param("departmentId") String departmentId,
                        Pageable pageable);

        // Tìm kiếm user theo tên, email hoặc personnel_id
        @Query("SELECT u FROM User u WHERE " +
                        "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.personnelId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<User> searchUsersByFullNameOrEmailOrPersonnelId(@Param("searchTerm") String searchTerm, Pageable pageable);

        Optional<User> findByPersonnelId(String personnelId);

        Optional<User> findByEmail(String email);

}