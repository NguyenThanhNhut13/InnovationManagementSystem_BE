package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "department_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String departmentId;

    @Column(name = "council_id", columnDefinition = "VARCHAR(36)")
    private String councilId;

    @Column(name = "template_id", columnDefinition = "VARCHAR(36)")
    private String templateId;

    @Column(name = "generated_pdf_path", columnDefinition = "TEXT")
    private String generatedPdfPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", columnDefinition = "VARCHAR(50)")
    private DocumentTypeEnum reportType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_data", columnDefinition = "JSON")
    private Map<String, Object> reportData;

    // Relationships
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();
}
