package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "innovation_decision")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDecision extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "decision_number", nullable = false, columnDefinition = "VARCHAR(100)")
    private String decisionNumber;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "promulgated_date", nullable = false, columnDefinition = "DATE")
    private LocalDate promulgatedDate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scoring_criteria", columnDefinition = "JSON")
    private JsonNode scoringCriteria;

    @Column(name = "content_guide", columnDefinition = "TEXT")
    private String contentGuide;

    // Relationships
    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InnovationRound> innovationRounds = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewScore> reviewScores = new ArrayList<>();
}