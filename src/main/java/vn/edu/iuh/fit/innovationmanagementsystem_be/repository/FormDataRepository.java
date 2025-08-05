package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormDataRepository extends JpaRepository<FormData, UUID> {

    // Basic CRUD operations
    Optional<FormData> findById(UUID id);

    List<FormData> findAll();

    void deleteById(UUID id);

    // Find by innovation ID
    List<FormData> findByInnovationId(UUID innovationId);

    // Find by form field ID
    List<FormData> findByFormFieldId(UUID formFieldId);

    // Find by innovation and form field
    Optional<FormData> findByInnovationIdAndFormFieldId(UUID innovationId, UUID formFieldId);

    // Find by value containing
    @Query("SELECT fd FROM FormData fd WHERE LOWER(fd.value) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FormData> findByValueContainingIgnoreCase(@Param("keyword") String keyword);

    // Find by innovation ID and form template ID
    @Query("SELECT fd FROM FormData fd WHERE fd.innovation.id = :innovationId AND fd.formField.formTemplate.id = :formTemplateId")
    List<FormData> findByInnovationIdAndFormTemplateId(@Param("innovationId") UUID innovationId,
            @Param("formTemplateId") UUID formTemplateId);

    // Count operations
    long countByInnovationId(UUID innovationId);

    long countByFormFieldId(UUID formFieldId);
}