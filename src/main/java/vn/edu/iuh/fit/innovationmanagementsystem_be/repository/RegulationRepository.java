package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Regulation;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegulationRepository extends JpaRepository<Regulation, String>, JpaSpecificationExecutor<Regulation> {

    // Tìm Regulation theo số hiệu điều
    Optional<Regulation> findByClauseNumber(String clauseNumber);

    // Tìm Regulation theo tiêu đề
    Optional<Regulation> findByTitle(String title);

    // Tìm tất cả Regulation theo InnovationDecision
    List<Regulation> findByInnovationDecisionId(String innovationDecisionId);

    // Tìm Regulation theo InnovationDecision với phân trang
    Page<Regulation> findByInnovationDecisionId(String innovationDecisionId, Pageable pageable);

    // Tìm tất cả Regulation theo Chapter
    List<Regulation> findByChapterId(String chapterId);

    // Tìm Regulation theo Chapter với phân trang
    Page<Regulation> findByChapterId(String chapterId, Pageable pageable);

    // Tìm Regulation không thuộc Chapter nào (null chapter_id)
    List<Regulation> findByChapterIdIsNull();

    // Tìm Regulation không thuộc Chapter nào với phân trang
    Page<Regulation> findByChapterIdIsNull(Pageable pageable);

    // Tìm kiếm Regulation theo từ khóa trong tiêu đề
    @Query("SELECT r FROM Regulation r WHERE r.title LIKE %:keyword%")
    Page<Regulation> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    // Tìm kiếm Regulation theo InnovationDecision và từ khóa
    @Query("SELECT r FROM Regulation r WHERE r.innovationDecision.id = :innovationDecisionId AND r.title LIKE %:keyword%")
    Page<Regulation> findByInnovationDecisionIdAndTitleContaining(
            @Param("innovationDecisionId") String innovationDecisionId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Tìm kiếm Regulation theo Chapter và từ khóa
    @Query("SELECT r FROM Regulation r WHERE r.chapter.id = :chapterId AND r.title LIKE %:keyword%")
    Page<Regulation> findByChapterIdAndTitleContaining(
            @Param("chapterId") String chapterId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Kiểm tra Regulation đã tồn tại theo số hiệu và InnovationDecision
    boolean existsByClauseNumberAndInnovationDecisionId(String clauseNumber, String innovationDecisionId);
}
