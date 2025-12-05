package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "generated_pdf_path", columnDefinition = "TEXT")
    private String generatedPdfPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", columnDefinition = "VARCHAR(50)")
    private DocumentTypeEnum reportType;

    // Relationships
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();
}
