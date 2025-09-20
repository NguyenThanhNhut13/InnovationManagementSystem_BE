package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;

import java.time.LocalDate;

@Repository
public interface InnovationDecisionRepository
                extends JpaRepository<InnovationDecision, String>, JpaSpecificationExecutor<InnovationDecision> {

        Page<InnovationDecision> findBySignedBy(String signedBy, Pageable pageable);

        boolean existsByDecisionNumber(String decisionNumber);

        @Query("SELECT id FROM InnovationDecision id WHERE id.promulgatedDate BETWEEN :startDate AND :endDate")
        Page<InnovationDecision> findByPromulgatedDateBetween(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);
}
