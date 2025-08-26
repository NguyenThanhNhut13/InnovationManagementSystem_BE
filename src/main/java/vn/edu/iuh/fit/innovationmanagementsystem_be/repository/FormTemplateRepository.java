package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;

import java.util.List;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, String> {

    // Tìm kiếm theo innovation round với phân trang
    Page<FormTemplate> findByInnovationRoundId(String innovationRoundId, Pageable pageable);

    // Tìm kiếm theo tên template (không phân biệt hoa thường)
    Page<FormTemplate> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Tìm kiếm theo innovation round và tên template
    @Query("SELECT ft FROM FormTemplate ft WHERE ft.innovationRound.id = :roundId AND LOWER(ft.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<FormTemplate> findByInnovationRoundIdAndNameContaining(
            @Param("roundId") String roundId,
            @Param("name") String name,
            Pageable pageable);

    // Kiểm tra template có được sử dụng bởi innovation nào không
    @Query("SELECT COUNT(fd) > 0 FROM FormData fd JOIN fd.formField ff JOIN ff.formTemplate ft WHERE ft.id = :templateId")
    boolean isTemplateUsedByInnovation(@Param("templateId") String templateId);

    // Lấy danh sách template theo innovation round
    List<FormTemplate> findByInnovationRoundIdOrderByName(String innovationRoundId);

    // Tìm kiếm tổng quát với nhiều tiêu chí
    @Query("SELECT ft FROM FormTemplate ft WHERE " +
            "(:roundId IS NULL OR ft.innovationRound.id = :roundId) AND " +
            "(:name IS NULL OR LOWER(ft.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<FormTemplate> findByCriteria(
            @Param("roundId") String roundId,
            @Param("name") String name,
            Pageable pageable);

    Page<FormTemplate> findAll(Specification<FormTemplate> specification, Pageable pageable);
}