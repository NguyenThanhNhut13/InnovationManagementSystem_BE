package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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

    @Column(name = "decision_number", nullable = false, unique = true, columnDefinition = "VARCHAR(100)")
    private String decisionNumber;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "promulgated_date", nullable = false, columnDefinition = "DATE")
    private LocalDate promulgatedDate;

    @Column(name = "signed_by", nullable = false, columnDefinition = "VARCHAR(255)")
    private String signedBy;

    @Column(name = "bases", columnDefinition = "TEXT")
    private String bases;

    // Relationships
    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Chapter> chapters = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Regulation> regulations = new ArrayList<>();

    // Other relationships (if any)
    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Innovation> innovations = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Council> councils = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormTemplate> formTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Regulation> regulationList = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();
}