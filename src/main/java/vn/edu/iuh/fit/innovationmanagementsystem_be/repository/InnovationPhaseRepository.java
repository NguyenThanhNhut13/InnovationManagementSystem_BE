package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InnovationPhaseRepository extends JpaRepository<InnovationPhase, String> {

        List<InnovationPhase> findByInnovationRoundIdOrderByPhaseOrder(String innovationRoundId);

        Optional<InnovationPhase> findByInnovationRoundIdAndPhaseType(String innovationRoundId,
                        InnovationPhaseEnum phaseType);

        @Query("SELECT p FROM InnovationPhase p WHERE p.innovationRound.id = :roundId " +
                        "AND p.isActive = true " +
                        "AND :currentDate >= p.phaseStartDate " +
                        "AND :currentDate <= p.phaseEndDate")
        Optional<InnovationPhase> findCurrentActivePhase(@Param("roundId") String roundId,
                        @Param("currentDate") LocalDate currentDate);

        void deleteByInnovationRoundId(String innovationRoundId);

        // New methods for phase status management
        List<InnovationPhase> findByPhaseStatus(PhaseStatusEnum phaseStatus);

        List<InnovationPhase> findByInnovationRoundIdAndPhaseStatus(String innovationRoundId,
                        PhaseStatusEnum phaseStatus);

        Optional<InnovationPhase> findByInnovationRoundIdAndPhaseOrder(String innovationRoundId, Integer phaseOrder);

        @Query("SELECT p FROM InnovationPhase p WHERE p.innovationRound.id = :roundId " +
                        "AND p.phaseStatus = :status " +
                        "ORDER BY p.phaseOrder")
        List<InnovationPhase> findByRoundIdAndStatusOrderByPhaseOrder(@Param("roundId") String roundId,
                        @Param("status") PhaseStatusEnum status);
}
