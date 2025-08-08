package vn.edu.iuh.fit.innovationmanagementsystem_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Attachment;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    // Tìm attachment theo innovation
    @Query("SELECT a FROM Attachment a WHERE a.innovation.id = :innovationId")
    List<Attachment> findByInnovationId(@Param("innovationId") String innovationId);

    // Tìm attachment theo loại
    List<Attachment> findByAttachmentType(AttachmentTypeEnum attachmentType);

    // Tìm attachment theo tên file
    List<Attachment> findByFileNameContaining(String fileName);

    // Đếm attachment theo innovation
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.innovation.id = :innovationId")
    long countByInnovationId(@Param("innovationId") String innovationId);

    // Đếm attachment theo loại
    long countByAttachmentType(AttachmentTypeEnum attachmentType);

    // Kiểm tra file path đã tồn tại chưa
    boolean existsByFilePath(String filePath);
}
