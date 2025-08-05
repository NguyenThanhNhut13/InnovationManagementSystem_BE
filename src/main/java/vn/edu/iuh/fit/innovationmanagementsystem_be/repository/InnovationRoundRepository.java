package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InnovationRoundRepository extends JpaRepository<InnovationRound, UUID> {

    // Basic CRUD operations
    Optional<InnovationRound> findById(UUID id);

    List<InnovationRound> findAll();

    void deleteById(UUID id);

    // Find by status
    List<InnovationRound> findByStatus(InnovationRound.InnovationRoundStatus status);

    // Find active rounds
    List<InnovationRound> findByStatusAndStartDateBeforeAndEndDateAfter(
            InnovationRound.InnovationRoundStatus status,
            LocalDate currentDate,
            LocalDate currentDate2);

    // Find by date range
    @Query("SELECT ir FROM InnovationRound ir WHERE ir.startDate <= :currentDate AND ir.endDate >= :currentDate")
    List<InnovationRound> findCurrentRounds(@Param("currentDate") LocalDate currentDate);

    // Search by name
    @Query("SELECT ir FROM InnovationRound ir WHERE LOWER(ir.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<InnovationRound> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Count operations
    long countByStatus(InnovationRound.InnovationRoundStatus status);
}