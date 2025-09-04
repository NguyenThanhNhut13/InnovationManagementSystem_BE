package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, String>, JpaSpecificationExecutor<Chapter> {

    Page<Chapter> findByInnovationDecisionId(String innovationDecisionId, Pageable pageable);

    boolean existsByChapterNumberAndInnovationDecisionId(String chapterNumber, String innovationDecisionId);
}
