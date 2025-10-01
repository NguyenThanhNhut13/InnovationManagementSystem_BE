package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.DepartmentPhase;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationPhaseEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentPhaseRepository extends JpaRepository<DepartmentPhase, String> {

        List<DepartmentPhase> findByDepartmentIdAndInnovationPhaseIdOrderByPhaseOrder(String departmentId,
                        String innovationPhaseId);

        Optional<DepartmentPhase> findByDepartmentIdAndInnovationPhaseIdAndPhaseType(String departmentId,
                        String innovationPhaseId, InnovationPhaseEnum phaseType);

        @Query("SELECT p FROM DepartmentPhase p WHERE p.department.id = :departmentId " +
                        "AND p.innovationPhase.id = :phaseId " +
                        "AND :currentDate >= p.startDate " +
                        "AND :currentDate <= p.endDate")
        Optional<DepartmentPhase> findCurrentActivePhase(@Param("departmentId") String departmentId,
                        @Param("phaseId") String phaseId,
                        @Param("currentDate") LocalDate currentDate);

        List<DepartmentPhase> findByInnovationPhaseIdOrderByDepartmentIdAscPhaseOrderAsc(String innovationPhaseId);

        boolean existsByDepartmentIdAndInnovationPhaseId(String departmentId, String innovationPhaseId);
}
