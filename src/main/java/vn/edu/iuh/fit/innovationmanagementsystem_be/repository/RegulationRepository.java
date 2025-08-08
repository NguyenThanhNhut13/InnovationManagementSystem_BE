package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegulationRepository extends JpaRepository<Regulation, String> {

        // Tìm regulation theo tên
        List<Regulation> findByRegulationNameContaining(String regulationName);

        // Tìm regulation theo innovation decision
        @Query("SELECT r FROM Regulation r WHERE r.innovationDecision.id = :decisionId")
        List<Regulation> findByInnovationDecisionId(@Param("decisionId") String decisionId);

        // Tìm regulation theo thời gian tạo
        @Query("SELECT r FROM Regulation r WHERE r.createdAt BETWEEN :startDate AND :endDate")
        List<Regulation> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tìm regulation theo thời gian hiệu lực
        @Query("SELECT r FROM Regulation r WHERE r.effectiveDate BETWEEN :startDate AND :endDate")
        List<Regulation> findByEffectiveDateBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tìm regulation đang hiệu lực
        @Query("SELECT r FROM Regulation r WHERE r.effectiveDate <= :currentDate AND (r.expiryDate IS NULL OR r.expiryDate >= :currentDate)")
        List<Regulation> findActiveRegulations(@Param("currentDate") LocalDateTime currentDate);

        // Đếm regulation theo innovation decision
        @Query("SELECT COUNT(r) FROM Regulation r WHERE r.innovationDecision.id = :decisionId")
        long countByInnovationDecisionId(@Param("decisionId") String decisionId);

        // Kiểm tra regulation name đã tồn tại chưa
        boolean existsByRegulationName(String regulationName);

        // Tìm regulation theo innovation round (thông qua innovation decision)
        @Query("SELECT r FROM Regulation r WHERE r.innovationDecision.id IN (SELECT id.id FROM InnovationDecision id JOIN id.innovationRounds ir WHERE ir.id = :roundId)")
        List<Regulation> findByInnovationRoundId(@Param("roundId") String roundId);
}
