package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.CoInnovation;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoInnovationRepository extends JpaRepository<CoInnovation, UUID> {

    List<CoInnovation> findByInnovationId(UUID innovationId);

    List<CoInnovation> findByUserId(UUID userId);

    @Query("SELECT ci FROM CoInnovation ci WHERE ci.innovation.id = :innovationId AND ci.user.id = :userId")
    CoInnovation findByInnovationIdAndUserId(@Param("innovationId") UUID innovationId, @Param("userId") UUID userId);

    boolean existsByInnovationIdAndUserId(UUID innovationId, UUID userId);

    void deleteByInnovationIdAndUserId(UUID innovationId, UUID userId);
}