package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewScoreRepository extends JpaRepository<ReviewScore, String> {

        // Tìm review score theo innovation
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.innovation.id = :innovationId")
        List<ReviewScore> findByInnovationId(@Param("innovationId") String innovationId);

        // Tìm review score theo council member
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.councilMember.id = :memberId")
        List<ReviewScore> findByCouncilMemberId(@Param("memberId") String memberId);

        // Tìm review score theo innovation decision
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.innovationDecision.id = :decisionId")
        List<ReviewScore> findByInnovationDecisionId(@Param("decisionId") String decisionId);

        // Tìm review score theo innovation và council member
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.innovation.id = :innovationId AND rs.councilMember.id = :memberId")
        List<ReviewScore> findByInnovationIdAndCouncilMemberId(@Param("innovationId") String innovationId,
                        @Param("memberId") String memberId);

        // Tìm review score theo điểm số
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.actualScore >= :minScore")
        List<ReviewScore> findByActualScoreGreaterThanEqual(@Param("minScore") Integer minScore);

        // Tìm review score theo khoảng điểm
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.actualScore BETWEEN :minScore AND :maxScore")
        List<ReviewScore> findByActualScoreBetween(@Param("minScore") Integer minScore,
                        @Param("maxScore") Integer maxScore);

        // Tìm review score theo thời gian tạo
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.createdAt BETWEEN :startDate AND :endDate")
        List<ReviewScore> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tính điểm trung bình của innovation
        @Query("SELECT AVG(rs.actualScore) FROM ReviewScore rs WHERE rs.innovation.id = :innovationId")
        Double getAverageScoreByInnovationId(@Param("innovationId") String innovationId);

        // Tìm review score theo innovation round (thông qua innovation)
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.innovation.innovationRound.id = :roundId")
        List<ReviewScore> findByInnovationRoundId(@Param("roundId") String roundId);

        // Tìm review score theo council (thông qua council member)
        @Query("SELECT rs FROM ReviewScore rs WHERE rs.councilMember.council.id = :councilId")
        List<ReviewScore> findByCouncilId(@Param("councilId") String councilId);
}
