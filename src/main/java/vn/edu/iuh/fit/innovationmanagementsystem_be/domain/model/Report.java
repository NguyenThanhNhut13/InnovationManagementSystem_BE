package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.DocumentTypeEnum;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "reports")
@NoArgsConstructor
@AllArgsConstructor
public class Report extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "applicable_year", nullable = false, columnDefinition = "INTEGER")
    private Integer applicableYear;

    @Column(name = "generated_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime generatedDate;

    @Column(name = "generated_pdf_path", columnDefinition = "TEXT")
    private String generatedPdfPath;

    @Column(name = "meeting_details", nullable = false, columnDefinition = "TEXT")
    private String meetingDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", columnDefinition = "VARCHAR(50)")
    private DocumentTypeEnum reportType;

    // Relationships
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReportInnovationDetail> reportInnovationDetails = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<DigitalSignature> digitalSignatures = new ArrayList<>();
}