package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "report_innovation_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportInnovationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "display_order")
    private Integer displayOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_fields", columnDefinition = "JSON")
    private String dataFields;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_id", nullable = false)
    private Innovation innovation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;
}