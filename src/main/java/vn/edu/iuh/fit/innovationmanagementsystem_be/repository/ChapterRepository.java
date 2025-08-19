package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Chapter;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, String>, JpaSpecificationExecutor<Chapter> {

    // Tìm Chapter theo số chương
    Optional<Chapter> findByChapterNumber(String chapterNumber);

    // Tìm Chapter theo tiêu đề
    Optional<Chapter> findByTitle(String title);

    // Tìm tất cả Chapter theo InnovationDecision
    List<Chapter> findByInnovationDecisionId(String innovationDecisionId);

    // Tìm Chapter theo InnovationDecision với phân trang
    Page<Chapter> findByInnovationDecisionId(String innovationDecisionId, Pageable pageable);

    // Tìm kiếm Chapter theo từ khóa trong tiêu đề
    @Query("SELECT c FROM Chapter c WHERE c.title LIKE %:keyword%")
    Page<Chapter> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    // Tìm kiếm Chapter theo InnovationDecision và từ khóa
    @Query("SELECT c FROM Chapter c WHERE c.innovationDecision.id = :innovationDecisionId AND c.title LIKE %:keyword%")
    Page<Chapter> findByInnovationDecisionIdAndTitleContaining(
            @Param("innovationDecisionId") String innovationDecisionId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Kiểm tra Chapter đã tồn tại theo số hiệu và InnovationDecision
    boolean existsByChapterNumberAndInnovationDecisionId(String chapterNumber, String innovationDecisionId);
}
