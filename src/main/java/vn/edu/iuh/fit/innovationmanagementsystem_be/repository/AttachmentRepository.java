package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    // Basic CRUD operations
    Optional<Attachment> findById(UUID id);

    List<Attachment> findAll();

    void deleteById(UUID id);

    // Find by innovation ID with pagination
    Page<Attachment> findByInitiativeId(UUID initiativeId, Pageable pageable);

    // Find by type with pagination
    Page<Attachment> findByType(Attachment.AttachmentType type, Pageable pageable);

    // Find by innovation ID and type with pagination
    Page<Attachment> findByInitiativeIdAndType(UUID initiativeId, Attachment.AttachmentType type, Pageable pageable);

    // Search by file name with pagination
    @Query("SELECT a FROM Attachment a WHERE LOWER(a.fileName) LIKE LOWER(CONCAT('%', :fileName, '%'))")
    Page<Attachment> findByFileNameContainingIgnoreCase(@Param("fileName") String fileName, Pageable pageable);

    // Search by MIME type with pagination
    @Query("SELECT a FROM Attachment a WHERE LOWER(a.mimeType) LIKE LOWER(CONCAT('%', :mimeType, '%'))")
    Page<Attachment> findByMimeTypeContainingIgnoreCase(@Param("mimeType") String mimeType, Pageable pageable);

    // Count operations
    long countByInitiativeId(UUID initiativeId);

    long countByType(Attachment.AttachmentType type);
}