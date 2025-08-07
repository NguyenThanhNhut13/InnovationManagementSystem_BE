package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.ReviewScore;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewScoreRepository extends JpaRepository<ReviewScore, UUID> {
    List<ReviewScore> findByInnovationId(UUID innovationId);

    List<ReviewScore> findByCouncilMemberId(UUID councilMemberId);

    List<ReviewScore> findByInnovationDecisionId(UUID decisionId);
}