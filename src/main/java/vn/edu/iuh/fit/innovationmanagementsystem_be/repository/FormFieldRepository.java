package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormField;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FieldTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, UUID> {

    // Basic CRUD operations
    Optional<FormField> findById(UUID id);

    List<FormField> findAll();

    void deleteById(UUID id);

    // Find by form template ID
    List<FormField> findByFormTemplateId(UUID formTemplateId);

    // Find by form template ID ordered by order index
    List<FormField> findByFormTemplateIdOrderByOrderIndex(UUID formTemplateId);

    // Find by field type
    List<FormField> findByFieldType(FieldTypeEnum fieldType);

    // Find by required field
    List<FormField> findByRequired(Boolean required);

    // Find by field key
    Optional<FormField> findByFieldKey(String fieldKey);

    // Find by label containing
    @Query("SELECT ff FROM FormField ff WHERE LOWER(ff.label) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FormField> findByLabelContainingIgnoreCase(@Param("keyword") String keyword);

    // Find by form template and field type
    List<FormField> findByFormTemplateIdAndFieldType(UUID formTemplateId, FieldTypeEnum fieldType);

    // Count operations
    long countByFormTemplateId(UUID formTemplateId);

    long countByFieldType(FieldTypeEnum fieldType);
}