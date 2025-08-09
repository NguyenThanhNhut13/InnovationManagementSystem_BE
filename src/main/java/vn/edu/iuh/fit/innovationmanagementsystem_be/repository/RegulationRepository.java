package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;

import java.util.List;

@Repository
public interface RegulationRepository extends JpaRepository<Regulation, String> {

        // Tìm regulation theo title
        List<Regulation> findByTitleContaining(String title);

        // Tìm regulation theo clause number
        List<Regulation> findByClauseNumberContaining(String clauseNumber);

        // Tìm regulation theo innovation decision
        @Query("SELECT r FROM Regulation r WHERE r.innovationDecision.id = :decisionId")
        List<Regulation> findByInnovationDecisionId(@Param("decisionId") String decisionId);

        // Đếm regulation theo innovation decision
        @Query("SELECT COUNT(r) FROM Regulation r WHERE r.innovationDecision.id = :decisionId")
        long countByInnovationDecisionId(@Param("decisionId") String decisionId);

        // Kiểm tra title đã tồn tại chưa
        boolean existsByTitle(String title);

        // Kiểm tra clause number đã tồn tại chưa
        boolean existsByClauseNumber(String clauseNumber);

        // Tìm regulation theo innovation round (thông qua innovation decision)
        @Query("SELECT r FROM Regulation r WHERE r.innovationDecision.id IN (SELECT id.id FROM InnovationDecision id JOIN id.innovationRounds ir WHERE ir.id = :roundId)")
        List<Regulation> findByInnovationRoundId(@Param("roundId") String roundId);
}
