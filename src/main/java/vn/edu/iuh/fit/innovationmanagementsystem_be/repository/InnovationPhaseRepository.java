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

        Optional<InnovationPhase> findFirstByInnovationRoundIdAndPhaseOrder(String innovationRoundId,
                        Integer phaseOrder);

        Optional<InnovationPhase> findFirstByInnovationRoundIdAndPhaseType(String innovationRoundId,
                        InnovationPhaseTypeEnum phaseType);

        @Query("SELECT ip FROM InnovationPhase ip " +
                        "JOIN ip.innovationRound ir " +
                        "WHERE ir.status = 'OPEN' AND ip.phaseType = :phaseType " +
                        "ORDER BY ip.phaseOrder ASC")
        List<InnovationPhase> findSubmissionPhasesByOpenRound(@Param("phaseType") InnovationPhaseTypeEnum phaseType);

        default Optional<InnovationPhase> findSubmissionPhaseByOpenRound(InnovationPhaseTypeEnum phaseType) {
                List<InnovationPhase> phases = findSubmissionPhasesByOpenRound(phaseType);
                return phases.isEmpty() ? Optional.empty() : Optional.of(phases.get(0));
        }

        /**
         * Tìm phase có isDeadline = true trong một round (bất kỳ phaseType nào)
         * Dùng để check deadline constraint khi tạo/cập nhật DepartmentPhase
         */
        Optional<InnovationPhase> findFirstByInnovationRoundIdAndIsDeadlineTrue(String innovationRoundId);

        default Optional<InnovationPhase> findPhaseWithDeadlineByRoundId(String roundId) {
                return findFirstByInnovationRoundIdAndIsDeadlineTrue(roundId);
        }

        /**
         * Tìm các phase có trạng thái SCHEDULED và đã đến ngày bắt đầu
         */
        @Query("SELECT ip FROM InnovationPhase ip " +
                        "WHERE ip.phaseStatus = 'SCHEDULED' " +
                        "AND ip.phaseStartDate <= :currentDate")
        List<InnovationPhase> findScheduledPhasesReadyToStart(@Param("currentDate") java.time.LocalDate currentDate);

        /**
         * Tìm các phase có trạng thái ACTIVE và đã qua ngày kết thúc
         */
        @Query("SELECT ip FROM InnovationPhase ip " +
                        "WHERE ip.phaseStatus = 'ACTIVE' " +
                        "AND ip.phaseEndDate < :currentDate")
        List<InnovationPhase> findActivePhasesReadyToComplete(@Param("currentDate") java.time.LocalDate currentDate);

        /**
         * Tìm các innovation phase SCORING cấp Trường đã kết thúc (endDate <
         * currentDate)
         * (để tự động cập nhật trạng thái innovation sau khi hết thời gian chấm điểm)
         * 
         * @param currentDate Ngày hiện tại
         */
        @Query("SELECT ip FROM InnovationPhase ip " +
                        "WHERE ip.phaseType = 'SCORING' " +
                        "AND ip.level = 'SCHOOL' " +
                        "AND ip.phaseEndDate < :currentDate")
        List<InnovationPhase> findSchoolScoringPhasesEnded(@Param("currentDate") java.time.LocalDate currentDate);
}
