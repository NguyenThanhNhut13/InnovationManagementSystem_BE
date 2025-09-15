package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InnovationPhaseRepository extends JpaRepository<InnovationPhase, String> {

    /**
     * Tìm tất cả giai đoạn của một InnovationRound theo thứ tự
     */
    List<InnovationPhase> findByInnovationRoundIdOrderByPhaseOrder(String innovationRoundId);

    /**
     * Tìm giai đoạn theo loại của một InnovationRound
     */
    Optional<InnovationPhase> findByInnovationRoundIdAndPhaseType(String innovationRoundId,
            InnovationPhaseEnum phaseType);

    /**
     * Tìm giai đoạn đang diễn ra của một InnovationRound
     */
    @Query("SELECT p FROM InnovationPhase p WHERE p.innovationRound.id = :roundId " +
            "AND p.isActive = true " +
            "AND :currentDate >= p.startDate " +
            "AND :currentDate <= p.endDate")
    Optional<InnovationPhase> findCurrentActivePhase(@Param("roundId") String roundId,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Tìm tất cả giai đoạn đang diễn ra
     */
    @Query("SELECT p FROM InnovationPhase p WHERE p.isActive = true " +
            "AND :currentDate >= p.startDate " +
            "AND :currentDate <= p.endDate")
    List<InnovationPhase> findAllCurrentActivePhases(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm giai đoạn đã hoàn thành của một InnovationRound
     */
    @Query("SELECT p FROM InnovationPhase p WHERE p.innovationRound.id = :roundId " +
            "AND p.isActive = true " +
            "AND :currentDate > p.endDate " +
            "ORDER BY p.phaseOrder")
    List<InnovationPhase> findCompletedPhases(@Param("roundId") String roundId,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Xóa tất cả giai đoạn của một InnovationRound
     */
    void deleteByInnovationRoundId(String innovationRoundId);

    /**
     * Kiểm tra xem có giai đoạn nào đang diễn ra không
     */
    @Query("SELECT COUNT(p) > 0 FROM InnovationPhase p WHERE p.innovationRound.id = :roundId " +
            "AND p.isActive = true " +
            "AND :currentDate >= p.startDate " +
            "AND :currentDate <= p.endDate")
    boolean hasActivePhase(@Param("roundId") String roundId, @Param("currentDate") LocalDate currentDate);
}
