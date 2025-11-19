package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    List<Attachment> findByInnovationId(String innovationId);

    Optional<Attachment> findByInnovationIdAndTemplateId(String innovationId, String templateId);

    void deleteByInnovationId(String innovationId);

    void deleteByInnovationIdAndTemplateId(String innovationId, String templateId);
}
