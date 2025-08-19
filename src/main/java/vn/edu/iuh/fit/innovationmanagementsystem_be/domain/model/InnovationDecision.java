package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "innovation_decision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InnovationDecision extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "decision_number", nullable = false)
    private String decisionNumber;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "promulgated_date", nullable = false)
    private LocalDate promulgatedDate;

    @Column(name = "signed_by", nullable = false)
    private String signedBy;

    @Column(name = "bases", columnDefinition = "TEXT")
    private String bases;

    // Relationships
    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InnovationRound> innovationRounds = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Regulation> regulations = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReviewScore> reviewScores = new ArrayList<>();

    @OneToMany(mappedBy = "innovationDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Chapter> chapters = new ArrayList<>();
}