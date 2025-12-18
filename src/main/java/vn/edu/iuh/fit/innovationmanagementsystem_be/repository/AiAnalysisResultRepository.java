package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.AiAnalysisResult;

import java.util.Optional;

@Repository
public interface AiAnalysisResultRepository extends JpaRepository<AiAnalysisResult, String> {

    @Query("SELECT a FROM AiAnalysisResult a WHERE a.innovationId = :innovationId ORDER BY a.createdAt DESC")
    Optional<AiAnalysisResult> findLatestByInnovationId(@Param("innovationId") String innovationId);

    @Query("SELECT a FROM AiAnalysisResult a WHERE a.innovationId = :innovationId AND a.contentHash = :contentHash ORDER BY a.createdAt DESC")
    Optional<AiAnalysisResult> findByInnovationIdAndContentHash(
            @Param("innovationId") String innovationId,
            @Param("contentHash") String contentHash);

    boolean existsByInnovationIdAndContentHash(String innovationId, String contentHash);

    void deleteByInnovationId(String innovationId);
}
