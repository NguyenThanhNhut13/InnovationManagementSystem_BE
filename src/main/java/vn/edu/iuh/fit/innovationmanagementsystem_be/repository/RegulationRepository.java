package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;

@Repository
public interface RegulationRepository extends JpaRepository<Regulation, String>, JpaSpecificationExecutor<Regulation> {

        Page<Regulation> findByInnovationDecisionId(String innovationDecisionId, Pageable pageable);

        Page<Regulation> findByChapterId(String chapterId, Pageable pageable);

        Page<Regulation> findByChapterIdIsNull(Pageable pageable);

        boolean existsByClauseNumberAndInnovationDecisionId(String clauseNumber, String innovationDecisionId);
}
