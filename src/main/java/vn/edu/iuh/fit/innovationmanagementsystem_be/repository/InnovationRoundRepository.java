package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InnovationRoundRepository
                extends JpaRepository<InnovationRound, String>, JpaSpecificationExecutor<InnovationRound> {

        List<InnovationRound> findByInnovationDecisionIdOrderByCreatedAtDesc(String innovationDecisionId);

        @Query("SELECT r FROM InnovationRound r WHERE r.innovationDecision.id = :decisionId " +
                        "AND :currentDate >= r.registrationStartDate " +
                        "AND :currentDate <= r.registrationEndDate")
        Optional<InnovationRound> findCurrentActiveRound(@Param("decisionId") String decisionId,
                        @Param("currentDate") LocalDate currentDate);

        List<InnovationRound> findByStatus(InnovationRoundStatusEnum status);

        @Query("SELECT r FROM InnovationRound r WHERE " +
                        ":currentDate >= r.registrationStartDate " +
                        "AND :currentDate <= r.registrationEndDate " +
                        "AND r.status = 'ACTIVE' " +
                        "ORDER BY r.createdAt DESC")
        Optional<InnovationRound> findCurrentActiveRound(@Param("currentDate") LocalDate currentDate);

}
