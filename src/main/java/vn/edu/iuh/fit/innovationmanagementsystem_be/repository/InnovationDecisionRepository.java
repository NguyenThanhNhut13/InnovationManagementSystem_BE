package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InnovationDecisionRepository extends JpaRepository<InnovationDecision, UUID> {

    // Basic CRUD operations
    Optional<InnovationDecision> findById(UUID id);

    List<InnovationDecision> findAll();

    void deleteById(UUID id);

    // Find by year
    List<InnovationDecision> findByYearDecision(Integer yearDecision);

    // Find by decision number
    Optional<InnovationDecision> findByDecisionNumber(String decisionNumber);

    // Find by innovation round
    @Query("SELECT id FROM InnovationDecision id WHERE id.innovationRound.id = :roundId")
    Optional<InnovationDecision> findByInnovationRoundId(@Param("roundId") UUID roundId);

    // Search by year range
    @Query("SELECT id FROM InnovationDecision id WHERE id.yearDecision BETWEEN :startYear AND :endYear")
    List<InnovationDecision> findByYearRange(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    // Count operations
    long countByYearDecision(Integer yearDecision);
}