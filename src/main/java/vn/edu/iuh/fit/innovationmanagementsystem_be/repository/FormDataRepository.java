package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormDataRepository extends JpaRepository<FormData, String> {

    // Basic CRUD
    List<FormData> findByInnovationId(String innovationId);

    List<FormData> findByFormFieldId(String formFieldId);

    Optional<FormData> findByInnovationIdAndFormFieldId(String innovationId, String formFieldId);

    // Search & Filter
    Page<FormData> findByInnovationId(String innovationId, Pageable pageable);

    List<FormData> findByInnovationIdAndFormFieldFormTemplateId(String innovationId, String templateId);

    List<FormData> findByFormFieldFormTemplateId(String templateId);

    // Business Logic
    @Query("SELECT COUNT(fd) FROM FormData fd WHERE fd.innovation.id = :innovationId AND fd.formField.formTemplate.id = :templateId")
    Long countByInnovationAndTemplate(@Param("innovationId") String innovationId,
            @Param("templateId") String templateId);

    @Query("SELECT COUNT(fd) FROM FormData fd WHERE fd.innovation.id = :innovationId AND fd.formField.formTemplate.id = :templateId AND fd.formField.isRequired = true")
    Long countRequiredFieldsByInnovationAndTemplate(@Param("innovationId") String innovationId,
            @Param("templateId") String templateId);

    @Query("SELECT COUNT(fd) FROM FormData fd WHERE fd.innovation.id = :innovationId AND fd.formField.formTemplate.id = :templateId AND fd.fieldValue IS NOT NULL AND fd.fieldValue != ''")
    Long countCompletedFieldsByInnovationAndTemplate(@Param("innovationId") String innovationId,
            @Param("templateId") String templateId);

    // Cross-entity queries
    @Query("SELECT fd FROM FormData fd JOIN fd.formField ff JOIN ff.formTemplate ft WHERE ft.innovationRound.id = :roundId ORDER BY ft.name, ff.orderInTemplate")
    List<FormData> findByInnovationRoundId(@Param("roundId") String roundId);

    @Query("SELECT fd FROM FormData fd JOIN fd.formField ff JOIN ff.formTemplate ft WHERE ft.innovationRound.id = :roundId AND fd.innovation.id = :innovationId ORDER BY ft.name, ff.orderInTemplate")
    List<FormData> findByInnovationRoundIdAndInnovationId(@Param("roundId") String roundId,
            @Param("innovationId") String innovationId);

    // Statistics queries
    @Query("SELECT ft.name, COUNT(fd) FROM FormData fd JOIN fd.formField ff JOIN ff.formTemplate ft WHERE fd.innovation.id = :innovationId GROUP BY ft.id, ft.name")
    List<Object[]> getFormDataStatisticsByInnovation(@Param("innovationId") String innovationId);

    // Validation queries
    @Query("SELECT ff FROM FormField ff WHERE ff.formTemplate.id = :templateId AND ff.isRequired = true AND ff.id NOT IN (SELECT fd.formField.id FROM FormData fd WHERE fd.innovation.id = :innovationId AND fd.fieldValue IS NOT NULL AND fd.fieldValue != '')")
    List<Object[]> getMissingRequiredFields(@Param("templateId") String templateId,
            @Param("innovationId") String innovationId);
}