package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
// import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

@Entity
@Table(name = "attachments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "path_url", columnDefinition = "TEXT")
    private String pathUrl;

    // @Enumerated(EnumType.STRING)
    // @Column(name = "type", columnDefinition = "VARCHAR(50)")
    // private AttachmentTypeEnum type;

    @Column(name = "file_name", columnDefinition = "VARCHAR(255)")
    private String fileName;

    @Column(name = "file_size", columnDefinition = "BIGINT")
    private Long fileSize;

    @Column(name = "template_id", columnDefinition = "VARCHAR(36)")
    private String templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", columnDefinition = "VARCHAR(50)")
    private DocumentTypeEnum documentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = Innovation.class)
    @JoinColumn(name = "innovation_id", nullable = false, referencedColumnName = "id")
    private Innovation innovation;
}