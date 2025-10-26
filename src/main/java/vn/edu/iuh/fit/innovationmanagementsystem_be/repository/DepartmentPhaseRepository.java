package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.PhaseStatusEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentPhaseRepository
        extends JpaRepository<DepartmentPhase, String>, JpaSpecificationExecutor<DepartmentPhase> {

    Optional<DepartmentPhase> findById(String id);

    List<DepartmentPhase> findByDepartmentId(String departmentId);

    List<DepartmentPhase> findByInnovationPhaseId(String innovationPhaseId);

    List<DepartmentPhase> findByDepartmentIdAndInnovationPhaseId(String departmentId, String innovationPhaseId);

    List<DepartmentPhase> findByPhaseStatus(PhaseStatusEnum phaseStatus);

    List<DepartmentPhase> findByDepartmentIdAndPhaseStatus(String departmentId, PhaseStatusEnum phaseStatus);

    Optional<DepartmentPhase> findByDepartmentIdAndInnovationRoundIdAndPhaseType(String departmentId,
            String innovationRoundId, InnovationPhaseTypeEnum phaseType);
}
