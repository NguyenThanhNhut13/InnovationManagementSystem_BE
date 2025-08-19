package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.AttachmentTypeEnum;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "path_url", nullable = false)
    private String pathUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AttachmentTypeEnum type;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    // Relationship - ManyToOne with Innovation (1 Innovation can have 0 or N
    // Attachments, but 1 Attachment must belong to 1 Innovation)
    @ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = Innovation.class)
    @JoinColumn(name = "innovation_id", nullable = false, referencedColumnName = "id")
    private Innovation innovation;
}