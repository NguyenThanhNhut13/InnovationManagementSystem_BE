package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface InnovationRoundRepository
                extends JpaRepository<InnovationRound, String>, JpaSpecificationExecutor<InnovationRound> {

        List<InnovationRound> findByStatus(InnovationRoundStatusEnum status);

        List<InnovationRound> findByAcademicYearAndNameIgnoreCase(String academicYear, String name);

        Optional<InnovationRound> findFirstByStatusOrderByCreatedAtDesc(InnovationRoundStatusEnum status);

        default Optional<InnovationRound> findByStatusOrderByCreatedAtDesc(InnovationRoundStatusEnum status) {
                return findFirstByStatusOrderByCreatedAtDesc(status);
        }

        Optional<InnovationRound> findFirstByOrderByCreatedAtDesc();

        default Optional<InnovationRound> findLatestRound() {
                return findFirstByOrderByCreatedAtDesc();
        }

        default Optional<InnovationRound> findCurrentActiveRound(java.time.LocalDate currentDate,
                        InnovationRoundStatusEnum status) {
                return findFirstByStatusOrderByCreatedAtDesc(status);
        }
}
