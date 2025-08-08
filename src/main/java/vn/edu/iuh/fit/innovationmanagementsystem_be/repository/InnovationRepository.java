package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InnovationRepository extends JpaRepository<Innovation, String> {

    // Tìm innovation theo tên
    List<Innovation> findByInnovationNameContaining(String innovationName);

    // Tìm innovation theo status
    List<Innovation> findByStatus(InnovationStatusEnum status);

    // Tìm innovation theo user
    @Query("SELECT i FROM Innovation i WHERE i.user.id = :userId")
    List<Innovation> findByUserId(@Param("userId") String userId);

    // Tìm innovation theo department
    @Query("SELECT i FROM Innovation i WHERE i.department.id = :departmentId")
    List<Innovation> findByDepartmentId(@Param("departmentId") String departmentId);

    // Tìm innovation theo innovation round
    @Query("SELECT i FROM Innovation i WHERE i.innovationRound.id = :roundId")
    List<Innovation> findByInnovationRoundId(@Param("roundId") String roundId);

    // Tìm innovation theo khoảng thời gian tạo
    @Query("SELECT i FROM Innovation i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<Innovation> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Tìm innovation theo user và status
    @Query("SELECT i FROM Innovation i WHERE i.user.id = :userId AND i.status = :status")
    List<Innovation> findByUserIdAndStatus(@Param("userId") String userId,
            @Param("status") InnovationStatusEnum status);

    // Tìm innovation theo department và status
    @Query("SELECT i FROM Innovation i WHERE i.department.id = :departmentId AND i.status = :status")
    List<Innovation> findByDepartmentIdAndStatus(@Param("departmentId") String departmentId,
            @Param("status") InnovationStatusEnum status);

    // Đếm innovation theo status
    long countByStatus(InnovationStatusEnum status);

    // Đếm innovation theo user
    @Query("SELECT COUNT(i) FROM Innovation i WHERE i.user.id = :userId")
    long countByUserId(@Param("userId") String userId);

    // Đếm innovation theo department
    @Query("SELECT COUNT(i) FROM Innovation i WHERE i.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") String departmentId);

    // Tìm innovation có điểm số
    @Query("SELECT i FROM Innovation i WHERE i.isScore = true")
    List<Innovation> findInnovationsWithScore();

    // Tìm innovation không có điểm số
    @Query("SELECT i FROM Innovation i WHERE i.isScore = false OR i.isScore IS NULL")
    List<Innovation> findInnovationsWithoutScore();
}

