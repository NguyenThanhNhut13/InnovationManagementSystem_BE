package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InnovationDecisionRepository extends JpaRepository<InnovationDecision, String> {

        // Tìm decision theo tên
        List<InnovationDecision> findByDecisionNameContaining(String decisionName);

        // Tìm decision theo thời gian tạo
        @Query("SELECT id FROM InnovationDecision id WHERE id.createdAt BETWEEN :startDate AND :endDate")
        List<InnovationDecision> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tìm decision theo thời gian hiệu lực
        @Query("SELECT id FROM InnovationDecision id WHERE id.effectiveDate BETWEEN :startDate AND :endDate")
        List<InnovationDecision> findByEffectiveDateBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tìm decision đang hiệu lực
        @Query("SELECT id FROM InnovationDecision id WHERE id.effectiveDate <= :currentDate AND (id.expiryDate IS NULL OR id.expiryDate >= :currentDate)")
        List<InnovationDecision> findActiveDecisions(@Param("currentDate") LocalDateTime currentDate);

        // Kiểm tra decision name đã tồn tại chưa
        boolean existsByDecisionName(String decisionName);

        // Tìm decision theo innovation round (thông qua quan hệ)
        @Query("SELECT id FROM InnovationDecision id JOIN id.innovationRounds ir WHERE ir.id = :roundId")
        List<InnovationDecision> findByInnovationRoundId(@Param("roundId") String roundId);
}
