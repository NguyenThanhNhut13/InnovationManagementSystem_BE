package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
