package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.enums.InnovationRoundStatusEnum;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Entity
@Table(name = "innovation_rounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InnovationRound extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "registration_start_date", nullable = false)
    private LocalDate registrationStartDate;

    @Column(name = "registration_end_date", nullable = false)
    private LocalDate registrationEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InnovationRoundStatusEnum status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innovation_decision_id", nullable = false)
    private InnovationDecision innovationDecision;

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FormTemplate> formTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Innovation> innovations = new ArrayList<>();

    @OneToMany(mappedBy = "innovationRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<InnovationPhase> innovationPhases = new HashSet<>();


    public boolean isPhaseWithinRoundTimeframe(LocalDate phaseStartDate,
            LocalDate phaseEndDate) {
        return !phaseStartDate.isBefore(registrationStartDate) && !phaseEndDate.isAfter(registrationEndDate);
    }
}
