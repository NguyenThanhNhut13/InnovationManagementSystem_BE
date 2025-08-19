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

        // Tìm Regulation theo InnovationDecision với phân trang
        Page<Regulation> findByInnovationDecisionId(String innovationDecisionId, Pageable pageable);

        // Tìm Regulation theo Chapter với phân trang
        Page<Regulation> findByChapterId(String chapterId, Pageable pageable);

        // Tìm Regulation không thuộc Chapter nào với phân trang
        Page<Regulation> findByChapterIdIsNull(Pageable pageable);

        // Kiểm tra Regulation đã tồn tại theo số hiệu và InnovationDecision
        boolean existsByClauseNumberAndInnovationDecisionId(String clauseNumber, String innovationDecisionId);
}
