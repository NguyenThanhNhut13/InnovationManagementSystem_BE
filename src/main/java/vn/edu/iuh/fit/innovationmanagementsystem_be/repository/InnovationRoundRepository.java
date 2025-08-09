package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InnovationRoundRepository extends JpaRepository<InnovationRound, String> {

        // Tìm round theo tên
        List<InnovationRound> findByNameContaining(String name);

        // Tìm round theo status
        List<InnovationRound> findByStatus(InnovationRoundStatusEnum status);

        // Tìm round theo khoảng thời gian start date
        List<InnovationRound> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

        // Tìm round theo khoảng thời gian end date
        List<InnovationRound> findByEndDateBetween(LocalDate startDate, LocalDate endDate);

        // Tìm round đang hoạt động
        @Query("SELECT ir FROM InnovationRound ir WHERE ir.status = 'ACTIVE'")
        List<InnovationRound> findActiveRounds();

        // Tìm round theo innovation decision
        @Query("SELECT ir FROM InnovationRound ir WHERE ir.innovationDecision.id = :decisionId")
        List<InnovationRound> findByInnovationDecisionId(@Param("decisionId") String decisionId);

        // Đếm round theo status
        long countByStatus(InnovationRoundStatusEnum status);

        // Đếm round theo innovation decision
        @Query("SELECT COUNT(ir) FROM InnovationRound ir WHERE ir.innovationDecision.id = :decisionId")
        long countByInnovationDecisionId(@Param("decisionId") String decisionId);

        // Tìm round theo thời gian bắt đầu và kết thúc
        @Query("SELECT ir FROM InnovationRound ir WHERE ir.startDate <= :currentDate AND ir.endDate >= :currentDate")
        List<InnovationRound> findCurrentRounds(@Param("currentDate") LocalDateTime currentDate);
}
