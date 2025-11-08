package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface InnovationPhaseRepository
                extends JpaRepository<InnovationPhase, String>, JpaSpecificationExecutor<InnovationPhase> {

        List<InnovationPhase> findByInnovationRoundIdOrderByPhaseOrder(String innovationRoundId);

        Optional<InnovationPhase> findById(String id);

        List<InnovationPhase> findByPhaseStatus(PhaseStatusEnum phaseStatus);

        List<InnovationPhase> findByInnovationRoundIdAndPhaseStatus(String innovationRoundId,
                        PhaseStatusEnum phaseStatus);

        Optional<InnovationPhase> findByInnovationRoundIdAndPhaseOrder(String innovationRoundId, Integer phaseOrder);

        Optional<InnovationPhase> findByInnovationRoundIdAndPhaseType(String innovationRoundId,
                        InnovationPhaseTypeEnum phaseType);

        @Query("SELECT ip FROM InnovationPhase ip " +
                        "JOIN ip.innovationRound ir " +
                        "WHERE ir.status = 'OPEN' AND ip.phaseType = :phaseType " +
                        "ORDER BY ip.phaseOrder ASC")
        Optional<InnovationPhase> findSubmissionPhaseByOpenRound(@Param("phaseType") InnovationPhaseTypeEnum phaseType);

        /**
         * Tìm phase có isDeadline = true trong một round (bất kỳ phaseType nào)
         * Dùng để check deadline constraint khi tạo/cập nhật DepartmentPhase
         */
        @Query("SELECT ip FROM InnovationPhase ip " +
                        "WHERE ip.innovationRound.id = :roundId " +
                        "AND ip.isDeadline = true")
        Optional<InnovationPhase> findPhaseWithDeadlineByRoundId(@Param("roundId") String roundId);
}
