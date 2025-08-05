package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InnovationRepository extends JpaRepository<Innovation, UUID> {

    // Basic CRUD operations
    Optional<Innovation> findById(UUID id);

    List<Innovation> findAll();

    void deleteById(UUID id);

    // Find by user ID with pagination
    Page<Innovation> findByUserId(UUID userId, Pageable pageable);

    // Find by department ID with pagination
    Page<Innovation> findByDepartmentId(UUID departmentId, Pageable pageable);

    // Find by status with pagination
    Page<Innovation> findByStatus(Innovation.InnovationStatus status, Pageable pageable);

    // Search by name with pagination
    @Query("SELECT i FROM Innovation i WHERE LOWER(i.innovationName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Innovation> findByInnovationNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Find by status group with pagination
    @Query("SELECT i FROM Innovation i WHERE " +
            "CASE " +
            "WHEN :statusGroup = 'draft' THEN i.status = 'DRAFT' " +
            "WHEN :statusGroup = 'submitted' THEN i.status IN ('SUBMITTED', 'PENDING_KHOA_REVIEW', 'RETURNED_TO_SUBMITTER') "
            +
            "WHEN :statusGroup = 'khoa_review' THEN i.status IN ('KHOA_REVIEWED', 'KHOA_APPROVED', 'KHOA_REJECTED') " +
            "WHEN :statusGroup = 'truong_review' THEN i.status IN ('PENDING_TRUONG_REVIEW', 'TRUONG_REVIEWED', 'TRUONG_APPROVED', 'TRUONG_REJECTED') "
            +
            "WHEN :statusGroup = 'final' THEN i.status = 'FINAL_APPROVED' " +
            "ELSE i.status IS NOT NULL " +
            "END")
    Page<Innovation> findByStatusGroup(@Param("statusGroup") String statusGroup, Pageable pageable);

    // Count operations
    long countByStatus(Innovation.InnovationStatus status);

    long countByUserId(UUID userId);

    long countByDepartmentId(UUID departmentId);
}