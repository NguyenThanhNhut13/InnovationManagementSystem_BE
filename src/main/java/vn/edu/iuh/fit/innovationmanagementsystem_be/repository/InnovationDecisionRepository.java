package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.InnovationDecision;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InnovationDecisionRepository
        extends JpaRepository<InnovationDecision, String>, JpaSpecificationExecutor<InnovationDecision> {

    // Tìm InnovationDecision theo số hiệu quyết định
    Optional<InnovationDecision> findByDecisionNumber(String decisionNumber);

    // Tìm InnovationDecision theo tiêu đề
    Optional<InnovationDecision> findByTitle(String title);

    // Tìm InnovationDecision theo người ký
    List<InnovationDecision> findBySignedBy(String signedBy);

    // Tìm InnovationDecision theo ngày ban hành
    List<InnovationDecision> findByPromulgatedDate(LocalDate promulgatedDate);

    // Tìm InnovationDecision theo năm
    List<InnovationDecision> findByYearDecision(Integer yearDecision);

    // Tìm kiếm InnovationDecision theo từ khóa trong tiêu đề hoặc số hiệu
    @Query("SELECT id FROM InnovationDecision id WHERE id.title LIKE %:keyword% OR id.decisionNumber LIKE %:keyword%")
    Page<InnovationDecision> findByTitleOrDecisionNumberContaining(@Param("keyword") String keyword, Pageable pageable);

    // Tìm kiếm InnovationDecision theo người ký với phân trang
    Page<InnovationDecision> findBySignedBy(String signedBy, Pageable pageable);

    // Tìm kiếm InnovationDecision theo năm với phân trang
    Page<InnovationDecision> findByYearDecision(Integer yearDecision, Pageable pageable);

    // Kiểm tra số hiệu quyết định đã tồn tại chưa
    boolean existsByDecisionNumber(String decisionNumber);

    // Tìm InnovationDecision theo khoảng thời gian ban hành
    @Query("SELECT id FROM InnovationDecision id WHERE id.promulgatedDate BETWEEN :startDate AND :endDate")
    Page<InnovationDecision> findByPromulgatedDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
