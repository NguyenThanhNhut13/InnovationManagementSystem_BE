package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewScoreRepository extends JpaRepository<ReviewScore, String> {

    // Kiểm tra user đã chấm điểm innovation này chưa
    boolean existsByInnovationIdAndReviewerId(String innovationId, String reviewerId);

    // Lấy điểm của user cho innovation
    Optional<ReviewScore> findByInnovationIdAndReviewerId(String innovationId, String reviewerId);

    // Lấy tất cả điểm cho innovation với fetch reviewer và userRoles
    @Query("SELECT rs FROM ReviewScore rs " +
            "LEFT JOIN FETCH rs.reviewer r " +
            "LEFT JOIN FETCH r.userRoles ur " +
            "LEFT JOIN FETCH ur.role " +
            "WHERE rs.innovation.id = :innovationId " +
            "ORDER BY rs.reviewedAt DESC, rs.createdAt DESC")
    List<ReviewScore> findByInnovationId(@Param("innovationId") String innovationId);

    // Đếm số lượng reviewer đã chấm điểm cho innovation
    long countByInnovationId(String innovationId);
}
