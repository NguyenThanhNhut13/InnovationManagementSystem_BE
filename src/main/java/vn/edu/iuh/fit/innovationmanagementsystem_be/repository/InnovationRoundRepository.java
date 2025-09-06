package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationRound;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.util.Optional;

@Repository
public interface InnovationRoundRepository
                extends JpaRepository<InnovationRound, String>, JpaSpecificationExecutor<InnovationRound> {

        Page<InnovationRound> findByStatus(InnovationRoundStatusEnum status, Pageable pageable);

        @Query("SELECT ir FROM InnovationRound ir " +
                        "LEFT JOIN FETCH ir.innovationDecision id " +
                        "WHERE ir.id = :id")
        Optional<InnovationRound> findByIdWithDecision(@Param("id") String id);

}
