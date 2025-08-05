package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.FormTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, UUID> {

    // Basic CRUD operations
    Optional<FormTemplate> findById(UUID id);

    List<FormTemplate> findAll();

    void deleteById(UUID id);

    // Find by name
    Optional<FormTemplate> findByName(String name);

    // Find by name containing
    @Query("SELECT ft FROM FormTemplate ft WHERE LOWER(ft.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FormTemplate> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Find by description containing
    @Query("SELECT ft FROM FormTemplate ft WHERE LOWER(ft.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FormTemplate> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Find by created by
    List<FormTemplate> findByCreatedBy(String createdBy);

    // Find by innovation round
    List<FormTemplate> findByInnovationRoundId(UUID innovationRoundId);

    // Count operations
    long countByCreatedBy(String createdBy);

    long countByInnovationRoundId(UUID innovationRoundId);
}