package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegulationRepository extends JpaRepository<Regulation, UUID> {

    // Basic CRUD operations
    Optional<Regulation> findById(UUID id);

    List<Regulation> findAll();

    void deleteById(UUID id);

    // Find by decision ID
    List<Regulation> findByInnovationDecisionId(UUID decisionId);

    // Find by clause number
    Optional<Regulation> findByClauseNumber(String clauseNumber);

    // Find by title
    @Query("SELECT r FROM Regulation r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Regulation> findByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Find by clause number containing
    @Query("SELECT r FROM Regulation r WHERE LOWER(r.clauseNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Regulation> findByClauseNumberContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Count operations
    long countByInnovationDecisionId(UUID decisionId);
}