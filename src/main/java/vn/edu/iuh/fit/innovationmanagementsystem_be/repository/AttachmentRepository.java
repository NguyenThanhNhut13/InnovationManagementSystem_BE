package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    List<Attachment> findByInnovationIdAndType(String innovationId, AttachmentTypeEnum type);

    Optional<Attachment> findByInnovationIdAndTemplateId(String innovationId, String templateId);

    void deleteByInnovationIdAndTemplateId(String innovationId, String templateId);
}

