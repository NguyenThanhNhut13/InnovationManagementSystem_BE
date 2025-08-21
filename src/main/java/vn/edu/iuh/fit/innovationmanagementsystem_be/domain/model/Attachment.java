package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;

@Entity
@Table(name = "attachments")
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "path_url", nullable = false, columnDefinition = "TEXT")
    private String pathUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "VARCHAR(50)")
    private AttachmentTypeEnum type;

    @Column(name = "file_name", columnDefinition = "VARCHAR(255)")
    private String fileName;

    @Column(name = "file_size", columnDefinition = "BIGINT")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = Innovation.class)
    @JoinColumn(name = "innovation_id", nullable = false, referencedColumnName = "id")
    private Innovation innovation;
}