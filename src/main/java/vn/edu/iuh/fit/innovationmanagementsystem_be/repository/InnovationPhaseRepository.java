package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface InnovationPhaseRepository extends JpaRepository<InnovationPhase, String> {

        List<InnovationPhase> findByInnovationDecisionIdOrderByPhaseOrder(String innovationDecisionId);

        Optional<InnovationPhase> findByInnovationDecisionIdAndPhaseType(String innovationDecisionId,
                        InnovationPhaseEnum phaseType);

        void deleteByInnovationDecisionId(String innovationDecisionId);

}
