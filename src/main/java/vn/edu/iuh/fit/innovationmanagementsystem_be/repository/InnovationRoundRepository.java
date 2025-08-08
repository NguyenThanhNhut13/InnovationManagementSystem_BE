package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InnovationRoundRepository extends JpaRepository<InnovationRound, String> {

    // Tìm round theo tên
    List<InnovationRound> findByRoundNameContaining(String roundName);

    // Tìm round theo status
    List<InnovationRound> findByStatus(InnovationRoundStatusEnum status);

    // Tìm round theo năm
    List<InnovationRound> findByYear(Integer year);

    // Tìm round theo khoảng thời gian
    @Query("SELECT ir FROM InnovationRound ir WHERE ir.startDate BETWEEN :startDate AND :endDate")
    List<InnovationRound> findByStartDateBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Tìm round đang hoạt động
    @Query("SELECT ir FROM InnovationRound ir WHERE ir.status = 'ACTIVE'")
    List<InnovationRound> findActiveRounds();

    // Tìm round theo innovation decision
    @Query("SELECT ir FROM InnovationRound ir WHERE ir.innovationDecision.id = :decisionId")
    List<InnovationRound> findByInnovationDecisionId(@Param("decisionId") String decisionId);

    // Tìm round theo năm và status
    @Query("SELECT ir FROM InnovationRound ir WHERE ir.year = :year AND ir.status = :status")
    List<InnovationRound> findByYearAndStatus(@Param("year") Integer year,
            @Param("status") InnovationRoundStatusEnum status);

    // Đếm round theo status
    long countByStatus(InnovationRoundStatusEnum status);

    // Đếm round theo năm
    long countByYear(Integer year);

    // Tìm round mới nhất theo năm
    @Query("SELECT ir FROM InnovationRound ir WHERE ir.year = :year ORDER BY ir.createdAt DESC")
    List<InnovationRound> findLatestRoundsByYear(@Param("year") Integer year);

    // Tìm round theo thời gian bắt đầu và kết thúc
    @Query("SELECT ir FROM InnovationRound ir WHERE ir.startDate <= :currentDate AND ir.endDate >= :currentDate")
    List<InnovationRound> findCurrentRounds(@Param("currentDate") LocalDateTime currentDate);
}

