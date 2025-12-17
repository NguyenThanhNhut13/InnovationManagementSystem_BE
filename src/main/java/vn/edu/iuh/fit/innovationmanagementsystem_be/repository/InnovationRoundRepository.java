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

        List<InnovationRound> findByStatus(InnovationRoundStatusEnum status);

        List<InnovationRound> findByAcademicYearAndNameIgnoreCase(String academicYear, String name);

        @Query("SELECT r FROM InnovationRound r WHERE r.status = :status " +
                        "ORDER BY r.createdAt DESC LIMIT 1")
        Optional<InnovationRound> findCurrentActiveRound(@Param("currentDate") LocalDate currentDate,
                        @Param("status") InnovationRoundStatusEnum status);

        @Query(value = "SELECT r FROM InnovationRound r WHERE r.status = :status " +
                        "ORDER BY r.createdAt DESC LIMIT 1")
        Optional<InnovationRound> findByStatusOrderByCreatedAtDesc(@Param("status") InnovationRoundStatusEnum status);

        @Query("SELECT r FROM InnovationRound r ORDER BY r.createdAt DESC LIMIT 1")
        Optional<InnovationRound> findLatestRound();

}
